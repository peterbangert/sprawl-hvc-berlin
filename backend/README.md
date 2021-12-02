# Sprawl Backend Controller

> ss2021 Interactive Performance Piece [sprawl.hvc.berlin](http://sprawl.hvc.berlin/)

## Description of Flask Backend component

The backend is built in Flask which is a python based web framework which is nice for building simple but robust APIs with and is ideal for this use case since it can import the relevant packages from Open Sound Control necessary to interact with the SPRAWL server.

The project has 3 interesting componenets, the header variables, the api call handlers, and the UWSGI implementation. 

### Header Variables

At the top of the `shb_backend.py` file you will first see the imports then a list of parameters shown below.

```
# Control Boundaries
MAX_REVERB = 3.0
MIN_REVERB = 0.0
MULTIPLIER_REVERB = 0.1
DEFAULT_REVERB = 0.1
MAX_DISTANCE = 10
MIN_DISTANCE = 0
MULTIPLIER_DISTANCE = 1.0
DEFAULT_DISTANCE = 1.0
MULTIPLIER_AZIMUTH = 0.25
DEFAULT_AZIMUTH = 1.0
MAX_GAIN = 5.0
MIN_GAIN = 0.5
DEFAULT_GAIN = 1.0
MULTIPLIER_GAIN = 0.2
MAX_ELEVATION = 20
MIN_ELEVATION = 0
MULTIPLIER_ELEVATION = 1.0
DEFAULT_ELEVATION = 0.0
```

These variables set the maximum and minimum bounds for the various controllable features from the frontend interface. Since the elevation, distance, rotations, gain, and reverb are all manipulable, the backend must monitor the state of these variables and as well asses whether requests exceed the desired limit. As well these variables define by what increment a variable may increase or decrease per call to the SPRAWL server by defining a MULTIPLIER variable. This MULTIPLIER is multiplied by 1 to determine the total incrementing amount. For instance, the variable `MULTIPLIER_AZIMUTH` is defined as 0.25, meaning when the azimuth (radians around circle counter-clockwise) is incremented positively or negatively by 0.25.

### API Call Handlers

The Nginx configuration will upstream requests made to `sprawl.hvc.berlin/api/v1` to the backend service which is listening on a socket interface at location `/home/student-super/sprawl-hvc-berlin/backend/backend.sock` 

Within the Backend application there are 4 api calls being handled.

1. `/control` : This api call will accept a Data body in the form below and perform an OSC call to the SPRAWL server, and as well updating a list of signal levels for each source.

```
{
    operation: [increase,decrease,reset],
    signal: [reverb,azimuth,distance,gain,elevation],
    source: [1..11]
}
```

2. `/results` : This api call will return a list of scores from every submitted result in the matching game

3. `/submit` : This api call will submit the matching guesses for the source matching game in the frontend

4. `/resetall` : This api call is important to use when restarting the sprawl server. This function will reset all levels for each source and signal back to zero, and the gain level to 0.5 (this is important to run because the gain level gets set to zero wen restarting the sprawl server and creates a noticable effect on the audio signal).


### uWSGI Implementation

Gunicorn is a uWSGI, a universial web server gateway interface, which is a calling convention for web servers to forward requests to web applications. Meaning, this tool will allow nginx to forward requests to the Flask application, this is accomplished because the uWSGI will create a unix socket file which the WSGI is bound to and will forward requests from, Nginx is already pre-configured to allow forwarding of requests to socket interfaces.

The `wsgi.py` file is an entry point for the Gunicorn uWSGI server to know how to start the overall application. See the README in the systemd directory for more information.