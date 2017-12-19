#!/bin/python3

import paho.mqtt.client as mqtt
import json
from enum import Enum, unique
from threading import Lock


crossing_robot = None
waiting_robot = None

mutex = Lock()


@unique
class Type(Enum):
    REQUEST = 1
    AUTORISATION = 2
    RELEASE = 3


# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

    client.subscribe("lejos/#")


# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    global crossing_robot
    global waiting_robot
    global mutex

    json_message = json.loads(msg.payload)

    mutex.acquire()
    
    print()
    print("=============================")
    print()
    print("robot UUID: " + json_message['UUID'])

    if json_message['type'] == Type.REQUEST.value:
        print('Request')
        if crossing_robot is None:
            print('Autorisation')
            crossing_robot = json_message['UUID']
        elif crossing_robot != json_message['UUID']:
            print('Waiting')
            waiting_robot = json_message['UUID']
    elif json_message['type'] == Type.RELEASE.value:
        print('Release')
        crossing_robot = None
        if waiting_robot is not None:
            print('Autorisation')
            crossing_robot = waiting_robot
            waiting_robot = None

    print("crossing_robot: " + str(crossing_robot))
    print("waiting_robot: " + str(waiting_robot))
    mutex.release()


client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect("172.20.50.106", 1883, 60)

# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
client.loop_forever()
