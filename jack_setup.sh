#!/bin/bash

sudo apt update
sudo apt install jackd2 qjackctl alsa-utils pulseaudio-module-jack # install JACK

sudo usermod -aG audio $USER
sudo reboot

aplay -l                                                           # get audio output card number
jackd -d alsa -d hw:0,0 -o2                                        # replace card number if necessary

arecord -l                                                         # get audio input card numbers
alsa_in -j mic -d dmic_sv -r 48000 -c2                              # replace card number if necessary
alsa_in -j media -d hw:3,0 -r 48000 -c6                         # replace card number if necessary

# Connections done upon starting python mixer, can also be done manually
# jack_connect mic:capture_1 system:playback_1 
# jack_connect mic:capture_2 system:playback_2 
# jack_connect scarlett:capture_1 system:playback_1 
# jack_connect scarlett:capture_2 system:playback_2 
