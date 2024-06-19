# 打印机程序

### 使用

1. 下载项目,打包程序
```shell
git clone https://github.com/booniez/printer

cd printer

mvn clean install
```
2. 将 ``.docker`` 内容上传到服务器(如果更改了项目，需要自行更新 jar 包)，并且执行
```shell
docker-compose -f docker-compose-printer.yml up -d --b
```


### 常见问题

1. 如果找不到打印机，怎么办？

部署在能访问到打印机的主机即可 ``java -Djava.security.egd=file:/dev/./urandom -jar ./printer/printer-0.0.1.jar``