How to start ?
==============

1. Start servers for dialoging with EV3 :
```
# Telechargement du serveur depuis le hub Docker :
$ docker run -it -p 1883:1883 -p 9001:9001 --network autonomous-lejos eclipse-mosquitto

# Construction du serveur python et lancement :
$ docker build -t autonomous-lejos-server . && docker run -it --rm --network autonomous-lejos autonomous-lejos-server --ip mosquitto
```
2. Build the jar file en send on the EV3 ...

 - ... on Manjaro :
`$ ./build.sh`
 - ... on Fedora :
 `$ ./build-fedora.sh`
 
 3. Start the program `autonomous-lejos.jar` on EV3 :
