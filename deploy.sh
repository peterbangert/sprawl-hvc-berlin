#!/bin/sh

# Deploy Nginx config
echo "Deploying Nginx Config ..."
cp nginx/sprawl-hvc-berlin /etc/nginx/sites-enabled/

echo "Reloading Nginx ..."
# Reload Nginx
systemctl reload nginx.service

echo "Starting Backend"
# Start Flask
python3 -m flask run --host=0.0.0.0 &

#sclang simple_SERVER.sc