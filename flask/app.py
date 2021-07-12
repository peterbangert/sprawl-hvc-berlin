from flask import Flask, request
from flask_restful import Resource, Api, reqparse
from flask_cors import CORS
import logging

from pythonosc import dispatcher
from pythonosc import osc_server, udp_client

# Super Collider Port and IP
SC_IP = "127.0.0.1"
SC_PORT = 57123

app = Flask(__name__)
api = Api(app,prefix="/api/v1")
CORS(app)

parser = reqparse.RequestParser()
parser.add_argument('operation')
parser.add_argument('signal')
parser.add_argument('source')
logging.basicConfig(filename='log/interactive-app_backend.log', level=logging.DEBUG)

# OSC dispatcher must be global
dispatcher = dispatcher.Dispatcher()

sources = {}

for i in range(16):
    sources[i] = {
        'reverb': 0,
        'gain': 0,
        'elevation': 0,
        'azimuth':0,
        'distance':0
    }

def send_osc(src, val, signal):
    app.logger.info("Sending osc update: {} {} {}".format(src,val,signal))
    client = udp_client.SimpleUDPClient(SC_IP, SC_PORT)
    if signal == 'azimuth':
        client.send_message("/source/azim", [src, val])
    elif signal == 'elevation':
        client.send_message("/source/elev", [src, val])
    elif signal == 'reverb':
        client.send_message("/source/reverb", [src, val])
    elif signal == 'distance':
        client.send_message("/source/dist", [src, val])
    elif signal == 'gain':
        client.send_message("/monitor/gain", [src, val])
    else:
        app.logger.info("FAILURE, Incorrect signal: {} {} {}".format(src,val,signal))

class Handler(Resource):
    def post(self):
        app.logger.info("Handling Request")
        args = parser.parse_args()

        #Sanity Check
        if args.operation != 'increase' and args.operation != 'decrease' and args.operation != 'reset':
            return {"Do operation {} on signal {} to source {}".format(args.operation,args.signal,args.source): 'failure'}


        apply_source = []
        if args.source == "All":
            apply_source = [x for x in range(16)]
        else:
            apply_source = [int(args.source)]

        app.logger.info("Applying on sources " + ','.join(str(e) for e in apply_source))

        for src in apply_source:
            
            new_value = sources[src][args.signal]    
            
            if args.operation == 'increase':
                new_value +=1
            elif args.operation == 'decrease':
                new_value -=1
            else :
                new_value = 0
            
            
            sources[src][args.signal] = new_value

            send_osc(src,new_value,args.signal)
            

        return {"Do operation {} on signal {} to source {}".format(args.operation,args.signal,args.source): 'successful'}

class GetSources(Resource):
    def get(self):
        app.logger.info("Retrieving Sources")
        return {"sources": 'N'}

api.add_resource(Handler,'/control')
api.add_resource(GetSources,'/sources')


if __name__ == '__main__':
    app.run(debug=True) 
