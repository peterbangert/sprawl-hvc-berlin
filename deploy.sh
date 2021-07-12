#!/bin/sh

# Deploy Nginx config
echo "Deploying Nginx Config ..."
cp nginx/sprawl-hvc-berlin /etc/nginx/sites-enabled/

echo "Reloading Nginx ..."
# Reload Nginx
systemctl reload nginx.service

# TODO: Start Flask

