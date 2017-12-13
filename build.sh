#!/bin/bash

LEJOS_LIB=$1
LEJOS_IP=$2

/usr/lib/jvm/java-7-openjdk/bin/javac -cp $LEJOS_LIB:lib/org.eclipse.paho.client.mqttv3-1.2.0.jar -d . src/*.java src/threads/*.java
jar -cvmf META-INF/MANIFEST.MF autonomous-lejos.jar *.class
rm *.class

ev3scpupload -n $LEJOS_IP lib/org.eclipse.paho.client.mqttv3-1.2.0.jar /home/lejos/lib/
ev3scpupload -n $LEJOS_IP autonomous-lejos.jar /home/lejos/programs
