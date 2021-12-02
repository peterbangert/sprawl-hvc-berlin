# Sprawl Nginx Configuration

> ss2021 Interactive Performance Piece [sprawl.hvc.berlin](http://sprawl.hvc.berlin/)


### Description

Nginx is an easy to use web server which can also be used as a revers proxy, load balancer, or HTTP cache. In this scenario we are using it to server web content and also reverse proxy (request forward) requests to our FLask python backend service.

The nginx configuration titled `sprawl-hvc-berlin` is placed inside the `/etc/nginx/sites-enabled/` directory. This directory is commonly where nginx configurations should be placed which should be served by the machine. Once placed, reload the nginx service to make public, `sudo systemctl reload nginx.service`

Inside the configuration there are 3 interesting components.


1. `server_name` : This line describes that requests made to the url: `http://sprawl.hvc.berlin` should be handled by this nginx configuration. 

2. `root /home/student-super/sprawl-hvc-berlin/public;` : This line defines that nginx should serve all content existent in this directory, which contains all the HTML/CSS/JS components visible in the webpage.

3. `location /api/v1` : This block of code describes that all requests made with the url path or `http://sprawl.hvc.berlin/api/v1/` should be forwarded to the unix socket which the backend service is listening on. These api requests are used to create the behind the scenes functionality of changing the audio signals for the different sources. 