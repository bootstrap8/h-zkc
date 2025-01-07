# User Guide
## 快速安装

- 构建
```bash
mvn -Dmaven.test.skip=true clean package -U
```

- 解压
```bash
cd target
tar xvf h-zkc-1.0-SHAPSHOT.tar.gz
```

- 配置
```bash
 cd h-zkc-1.0-SHAPSHOT/deploy/bootstrap/config
 vim application-mysql.yml
```

```yml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    dynamic:
      enabled: true
      base-packages: com.github.hbq969
      default-lookup-key: hikari
    hikari:
      jdbc-url: jdbc:mysql://localhost:3306/xxx?useUnicode=true&allowPublicKeyRetrieval=true&characterEncoding=utf-8&useSSL=false&autoReconnect=true&failOverReadOnly=false&maxReconnects=15000
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: xxx
      password: xxx
      maximum-pool-size: 10
      minimum-idle: 2
      max-lifetime: 1800000
      connection-test-query: SELECT 1

mybatis:
  mapper-locations:
    - classpath*:mappers/*.xml
    - classpath*:**/mapper/common/*Mapper.xml
    - classpath*:**/mapper/mysql/*Mapper.xml
  config-location: classpath:jpaConfig-mysql.xml

zkc:
  urls: localhost:2181
  dialect: mysql
```

- 部署
```bash
cd h-zkc-1.0-SHAPSHOT/deploy/bootstrap
sh start.sh
```

# 功能展示
![功能展示](src/main/resources/static/src/assets/readme-login.png)

![功能展示](src/main/resources/static/src/assets/readme-main.png)

![功能展示](src/main/resources/static/src/assets/readme-adddir.png)

![功能展示](src/main/resources/static/src/assets/readme-addprop.png)

![功能展示](src/main/resources/static/src/assets/readme-delprpo.png)

![功能展示](src/main/resources/static/src/assets/readme-import.png)

![功能展示](src/main/resources/static/src/assets/readme-query.png)

![功能展示](src/main/resources/static/src/assets/readme-history.png)

![功能展示](src/main/resources/static/src/assets/readme-backup.png)

![功能展示](src/main/resources/static/src/assets/readme-role.png)

![功能展示](src/main/resources/static/src/assets/readme-user.png)

![功能展示](src/main/resources/static/src/assets/readme-menu.png)