FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/java:17-anolis

# 更新包列表并安装CUPS
RUN yum update -y && yum install -y cups

VOLUME /tmp
ADD printer-0.0.1.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
