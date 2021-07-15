from flask import Flask, request
from flask_restful import Resource, Api, reqparse
from flask_cors import CORS
import logging
import os

from pythonosc import dispatcher
from pythonosc import osc_server, udp_client

# Super Collider Port and IP
SC_IP = "127.0.0.1"
SC_PORT = 57121

# Control Boundaries
MAX_REVERB = 1.0
MIN_REVERB = 0.0
MULTIPLIER_REVERB = 0.1
MAX_DISTANCE = 50
MIN_DISTANCE = -50
MULTIPLIER_DISTANCE = 1.0
MULTIPLIER_AZIMUTH = 0.25

# Create App
app = Flask(__name__)
api = Api(app,prefix="/api/v1")
CORS(app)

# Arguments for control API
signal_args = reqparse.RequestParser()
signal_args.add_argument('operation')
signal_args.add_argument('signal')
signal_args.add_argument('source',type=int)


# Arguments for control API
submit_args = reqparse.RequestParser()
submit_args.add_argument('name')
submit_args.add_argument('0')
submit_args.add_argument('1')
submit_args.add_argument('2')
submit_args.add_argument('3')
submit_args.add_argument('4')
submit_args.add_argument('5')
submit_args.add_argument('6')
submit_args.add_argument('7')



# Setup Logging
logfile = 'log/shb_backend.log'
basedir = os.path.dirname(logfile)
if not os.path.exists(basedir):
    os.makedirs(basedir)
open(logfile,'a').close()
logging.basicConfig(filename=logfile, level=logging.DEBUG)

# OSC dispatcher must be global
dispatcher = dispatcher.Dispatcher()

sources = {}

for i in range(8):
    sources[i] = {
        'reverb': 0,
        'azimuth':0,
        'distance':0
    }

solution = ["Ben","Peter","Valentin","Simon","Laurin","Luzie","Roman","Christian"]
results = {}

class SignalController(Resource):
    def post(self):
        app.logger.info("Handling Request")
        args = signal_args.parse_args()

        #Sanity Check
        if args.operation != 'increase' and args.operation != 'decrease' and args.operation != 'reset':
            return {"Do operation {} on signal {} to source {}".format(args.operation,args.signal,args.source): 'failure'}

        current_value = sources[args.source][args.signal]
        endpoint = ""
        additive = 0

        if args.operation == 'increase':
            additive =1 
        elif args.operation == 'decrease':
            additive =-1
        else :
            current_value = 0

        if args.signal == 'azimuth':
            endpoint = "/source/azim"
            additive = additive * MULTIPLIER_AZIMUTH
        elif args.signal == 'reverb':
            endpoint = "/source/reverb"
            additive = additive * MULTIPLIER_REVERB
            if current_value + additive > MAX_REVERB or current_value + additive < MIN_REVERB:
                additive =  0
        elif args.signal == 'distance':
            endpoint = "/source/dist"
            additive = additive * MULTIPLIER_DISTANCE
            if current_value + additive > MAX_DISTANCE or current_value + additive < MIN_DISTANCE:
                additive =  0
        else:
            app.logger.info("FAILURE, Incorrect signal: {} {} {}".format(args.operation,args.signal,str(args.source)))

        
        new_value = current_value + additive
        sources[args.source][args.signal] = new_value


        app.logger.info("Sending osc update: {} {} {}".format(args.source,new_value,args.signal))
        client = udp_client.SimpleUDPClient(SC_IP, SC_PORT)

        client.send_message(endpoint, [(args.source-1) *2, new_value])
        client.send_message(endpoint, [(args.source-1) *2 +1, new_value])
            

        return {"Do operation {} on signal {} to source {}".format(args.operation,args.signal,str(args.source)): 'successful'}

class GetResults(Resource):
    def get(self):
        app.logger.info("Retrieving Sources")
        return results


class PostSubmit(Resource):
    def post(self):
        app.logger.info("Submitting Results")
        args = submit_args.parse_args()
        app.logger.info(args)
        score = 0
        for i in range(len(solution)):
            if solution[i] == args[str(i)]:
                score +=1
        results[args.name] = score / 8

        return {"Submissions": "succesful"}


api.add_resource(SignalController,'/control')
api.add_resource(GetResults,'/results')
api.add_resource(PostSubmit,'/submit')


if __name__ == '__main__':
    app.run(debug=True) 
