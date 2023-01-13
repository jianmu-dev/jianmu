FROM nginx:1.21.0
WORKDIR /home/jianmu/
COPY nginx-http.conf /etc/nginx/conf.d/default.conf
COPY version .
ADD jianmu-ui.tar.gz ./