#!/bin/sh

# Deploy Nginx config
cp nginx/sprawl-hvc-berlin /etc/nginx/sites-enabled/

# Reload Nginx
systemctl reload nginx.service

# TODO: Start Flask

