deploy:
	${MAKE} deploy-nginx
	${MAKE} restart-backend
	sleep 5
	${MAKE} reset-signals

deploy-nginx:
	sudo cp nginx/sprawl-hvc-berlin /etc/nginx/sites-enabled/
	sudo systemctl reload nginx.service

restart-backend:
	sudo systemctl restart shb_backend

reset-signals:
	curl -X POST sprawl.hvc.berlin/api/v1/resetall

setup:
	cp systemd/shb_backend.service /etc/systemd/system
	sudo systemctl daemon-reload
	sudo systemctl start shb_backend