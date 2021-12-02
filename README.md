# Sprawl Audience Control

> ss2021 Interactive Performance Piece [sprawl.hvc.berlin](http://sprawl.hvc.berlin/)




### Description

The Audience Control project is a project designed to introduce randomness in live performances over the SPRAWL system by allowing the audience to anonymously control audio signals.


1. First, visit [sprawl.hvc.berlin](http://sprawl.hvc.berlin/) and select one of the 11 various performers on the top of the screen by the numbered buttons
2. The controls will adjust the spatial signals of the respective performer, the options being:
    - Reverb controls can increase or decrease the amount of signal reflection in the acoustic space.
    - Gain controls the input of the digital source, overall effects distortion. 
    - Distance controls virtually how far the performer is from a center point
    - Elevation controls height from center point.
    - Rotate controls at which angle the performer is from 0 degrees.
    
3. Guess which audio source belongs to which performer shown on screen, and the results will be displayed when the performance is complete.

    - The performance piece will have different performers playing at different times, which will allow viewers a more easy opportunity to determine which sources belong with which users.


<p align="center">
  <img width="650" height="300" src="public/images/diagram.png">
</p>

### How to Setup

On the host machine install:

1. [nginx](https://www.nginx.com/resources/wiki/start/topics/tutorials/install/)

2. Flask and Gunicorn

  `python3 -m pip install Flask gunicorn`

3. Install this repository in your home directory. 
  - the current configurations are designed to work with username student-super

4. Run `make setup`

### How to Deploy

Run `make deploy`

