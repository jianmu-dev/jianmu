FROM openjdk:11.0.16-jdk
WORKDIR /home/jianmu/
ENV PATH="/home/jianmu:${PATH}"
COPY wait-for-it.sh .
COPY jianmu-server.jar .
COPY version .
# 兼容旧版本
RUN ["ln", "-s", "/home/jianmu/data", "/ci"]
RUN ["chmod", "+x", "wait-for-it.sh"]
