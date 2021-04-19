#! /bin/bash
docker build -t miro.com/widget-service .
docker run -d -p 8080:8080 miro.com/widget-service
