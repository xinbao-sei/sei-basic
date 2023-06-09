FROM openjdk:8-jre-alpine

# 作者
LABEL maintainer="hua.feng@changhong.com"

# 环境变量 JAVA_OPTS：JAVA启动参数 APP_NAME：应用名称（各项目需要修改）
ENV JAVA_OPTS=""  APP_NAME="sei-basic"

# 设置时区
RUN rm -rf /etc/localtime && ln -s /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

# 添加应用
ADD $APP_NAME-v6-service/build/libs/$APP_NAME-v6.jar $APP_NAME.jar

# 开放8080端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["sh","-c","java -server -XX:InitialRAMPercentage=75.0  -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC $JAVA_OPTS -jar $APP_NAME.jar --server.servlet.context-path=/$APP_NAME"]
