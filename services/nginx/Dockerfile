FROM nginx:latest
  
COPY services/nginx/nginx.conf /etc/nginx/nginx.conf
ADD keys /etc/letsencrypt
ADD public /usr/share/nginx/html
