FROM docker.jianmuhub.com/library/nginx:1.22.1-alpine
WORKDIR /home/jianmu/
COPY nginx-http.conf /etc/nginx/conf.d/default.conf
COPY version .
ADD jianmu-ui.tar.gz ./