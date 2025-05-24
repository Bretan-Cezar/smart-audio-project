#!/bin/bash

sudo apt update
sudo apt install jackd2 qjackctl alsa-utils pulseaudio-module-jack # install JACK

sudo usermod -aG audio $USER
sudo reboot

arecord -l # see device indices

jackd -d alsa -d hw:0,0 -o2 # start JACK server on playback device
alsa_in -j mic -d hw:4,0 -r 48000 -c2 # start ALSA input
alsa_in -j scarlett -d hw:3,0 -r 48000 -c6 # start ALSA input

jack_connect mic:capture_1 system:playback_1 # 
jack_connect mic:capture_2 system:playback_2 
jack_connect scarlett:capture_1 system:playback_1 
jack_connect scarlett:capture_2 system:playback_2 
