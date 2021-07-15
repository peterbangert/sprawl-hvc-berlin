#!/bin/sh

# Deploy Nginx config
echo "Deploying Nginx Config ..."
sudo cp nginx/sprawl-hvc-berlin /etc/nginx/sites-enabled/

echo "Reloading Nginx ..."
# Reload Nginx
sudo systemctl reload nginx.service

echo "Starting Backend"
# Start Flask
sudo systemctl restart shb_backend

#sclang simple_SERVER.sc
