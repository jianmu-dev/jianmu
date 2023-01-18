FROM docker.jianmuhub.com/library/corretto:11.0.17
WORKDIR /home/jianmu/
ENV PATH="/home/jianmu:${PATH}"
COPY wait-for-it.sh .
COPY jianmu-server.jar .
COPY version .
# 兼容旧版本
RUN ["ln", "-s", "/home/jianmu/data", "/ci"]
RUN ["chmod", "+x", "wait-for-it.sh"]
ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai",  "-jar", "jianmu-server.jar"]