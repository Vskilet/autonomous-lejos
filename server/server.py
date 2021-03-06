#!/bin/python3

import paho.mqtt.client as mqtt
import paho.mqtt.publish as publish
import json
from enum import Enum, unique
from threading import Lock
import argparse
from collections import deque

parser = argparse.ArgumentParser(description='MQTT Lejos Server')
parser.add_argument('--ip', required=True,
                    help='IP Address of the MQTT server')

args = parser.parse_args()


crossing_robot = None
waiting_robots = deque()

mutex = Lock()


@unique
class Type(Enum):
    REQUEST = 1
    AUTORISATION = 2
    RELEASE = 3


def generate_autorisation(uuid):
    publish.single("lejos/autorisation",
                   json.dumps({"type": Type.AUTORISATION.value, "uuid": uuid}),
                   hostname=args.ip)


# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

    client.subscribe("lejos/request")


# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    global crossing_robot
    global waiting_robots
    global mutex

    json_message = json.loads(msg.payload)
    robot_uuid = json_message['uuid']

    mutex.acquire()

    print()
    print("=============================")
    print()
    print("robot UUID: " + robot_uuid)

    if json_message['type'] == Type.REQUEST.value:
        print('Request')
        if crossing_robot is None:
            print('Autorisation')
            crossing_robot = robot_uuid
            generate_autorisation(crossing_robot)
        elif crossing_robot != robot_uuid:
            print('Waiting')
            waiting_robots.append(robot_uuid)
        elif crossing_robot == robot_uuid:
            print('Autorisation')
            crossing_robot = robot_uuid
            generate_autorisation(crossing_robot)
    elif json_message['type'] == Type.RELEASE.value:
        print('Release')
        if crossing_robot == robot_uuid:
            crossing_robot = None
            if waiting_robots:
                print('Autorisation')
                crossing_robot = waiting_robots.popleft()
                generate_autorisation(crossing_robot)

    print("crossing_robot: " + str(crossing_robot))
    print("waiting_robots: " + str(waiting_robots))
    mutex.release()


client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect(args.ip, 1883, 60)

# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
client.loop_forever()
