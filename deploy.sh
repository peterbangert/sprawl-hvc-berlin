#!/bin/sh

# Deploy Nginx config
echo "Deploying Nginx Config ..."
sudo cp nginx/sprawl-hvc-berlin /etc/nginx/sites-enabled/

echo "Reloading Nginx ..."
# Reload Nginx
sudo systemctl reload nginx.service

echo "Starting Backend"
# Start Flask
(cd flask; python3 -m flask run --host=0.0.0.0 & )

#sclang simple_SERVER.sc
