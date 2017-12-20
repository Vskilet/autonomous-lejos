#!/bin/bash
 
LEJOS_LIB=$1
LEJOS_IP=$2

jar -uvmf META-INF/MANIFEST.MF autonomous-lejos.jar
ev3scpupload -n $LEJOS_IP lib/org.eclipse.paho.client.mqttv3-1.2.0.jar /home/lejos/lib/
ev3scpupload -n $LEJOS_IP lib/gson-2.8.2.jar /home/lejos/lib/
ev3scpupload -n $LEJOS_IP autonomous-lejos.jar /home/lejos/programs

