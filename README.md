# 事件管理系统

***

## 系统介绍

创建一个与事件管理相关的简单应用程序。该应用程序将列出所有现有事件，用户可以添加、删除和修改事件。

在线演示访问地址（腾讯云轻量化服务器2c4g配置，docker-compose部署）：

访问路径 : https://212.64.26.23:30092/  

用户名：admin@example.com 

密码：123456

***

## 设计方案

### **架构风格**:

*   **RESTful API**: 使用 RESTful 风格设计 API，提供标准化的接口以便于客户端交互。
*   **分层架构**: 划分为表示层、业务逻辑层和数据访问层，以实现良好的解耦和可维护性。
    
    ***

### **技术栈**:

* **后端**: Java 17, Spring Boot，Spring Data JPA

* **前端**: Vite + React

* **构建工具**: Maven

*   **容器化**: Docker, Docker-compose ,K8S
***

### **前端功能**

*   事件分页列表，搜索

*   事件添加

*   事件编辑

*   事件删除

    ***

### **数据设计**：

数据库：使用轻量级H2 数据库，可以在内存模式下运行

主要实体: Incident

字段 :

*   `id`: 唯一标识符
*   `title`: 事件标题（添加索引）
*   `description`: 事件描述
*   `status`: 事件状态（如 Open, In Progress, Resolved,Closed）
*   `createdAt`: 创建时间
*   `updatedAt`: 修改时间
    
    ***

### **API 设计**：

#### **1.接口设计**:

*   GET /api/incidents

    事件分页列表：

    *   支持参数: 分页（`page` 和 `size`）、模糊查询（`title`）
    *   返回: 事件分页列表
*   GET /api/incidents/{id}

    事件详情：

    *   支持参数: 事件 ID
    *   返回: 事件详情
*   POST /api/incidents

    创建事件：

    *   请求体: `Incident` 对象
    *   返回: 创建的事件
*   PUT /api/incidents/{id}

    修改事件：

    *   参数: 事件 ID
    *   请求体: 更新的 `Incident` 对象
    *   返回: 更新后的事件
*   DELETE /api/incidents/{id}

    删除事件：

    *   参数: 事件 ID
    *   返回: 操作结果状态

#### 2.**验证和异常处理**:

    *   集成spring-boot-starter-validation进行参数校验，并在统一异常处理中解析校验错误信息返回。
    *   统一的异常处理机制，自定义异常、封装统一返回结果，返回标准化格式的错误响应。
***
### **缓存机制**：

#### 1、Spring Cache Caffeine;

    *   使用Spring Cache， 使用 `@Cacheable`, `@CachePut`, `@CacheEvict` 和 `@Caching` 注解来管理缓存。
    *   配置了 Caffeine 作为本地缓存，Caffeine 默认使用 TinyLfu 作为缓存淘汰策略，这是一种高效的近似 LFU 策略,并设置了缓存的最大容量和过期时间,可以更好地控制缓存的性能和一致性，并提高系统的性能和响应速度。

#### 2、三缓问题处理：

    *   1.为防止缓存穿透，添加了基于 Guava的布隆过滤器 ，在查询数据库之前，先用布隆过滤器检查键是否存在，在创建和更新缓存项时，更新布隆过滤器。
    *   2.为防止缓存击穿， 使用ReentrantLock锁机制来实现排他锁，使用 `ConcurrentHashMap` 来存储每个 ID 对应的 `ReentrantLock`，并在需要时获取锁，手动缓存管理，为了防止死锁在 ReentrantLock 的 tryLock 方法中设置超时时间，并添加获取锁重试3次机制。
    *   3.为防止缓存雪崩，通过设置随机过期时间，防止大量的缓存在同一时间失效，导致大量请求直接访问数据库。


#### 2、双写一致性问题处理：

    *   引入延迟双删策略，在更新数据库后，先删除缓存，然后在短暂延迟后再次删除缓存。最大化地捕获期间的脏读取。

### **日志优化**：

    *   异步日志处理 ，将日志写入操作异步化，通过 `queueSize` 设置日志队列大小，可以缓冲大量日志条目，减少主线程的阻塞时间，提高应用程序的整体性能。
    *   支持日志级别灵活配置，自动按时间滚动生成日志文件，设置了日志文件大小限制，历史日志保留时间。
    *   请求跟踪，通过LogFilter向每个请求都添加一个唯一的请求 ID，便于在整个请求生命周期中跟踪，提供了丰富的上下文信息，便于问题定位和调试，使用 MDC 管理请求 ID，确保线程安全。
    *   统一格式， 确保所有日志条目遵循相同的格式，便于管理和分析。

### **全面测试**：

#### 1、单元测试： 
    使用 JUnit 和 Mockito 对服务层和控制器层进行单元测试。保证服务层和控制器层单元测试单元测试覆盖率。


#### 2、压力测试：




### **部署方案**：

#### 1、 Docker部署
- 配置 Dockerfile 构建前后台镜像，并启动。

- 后台DockerFile文件：

  
       FROM openjdk:17-jdk-alpine
          
       WORKDIR /app
          
       COPY incident-management.jar app.jar
          
       EXPOSE 8080
          
       ENTRYPOINT ["java", "-jar", "app.jar"] 

- 后台打镜像指令：
    
 docker build --no-cache --build-arg jar_file=incident-management.jar -t incident-management:latest .
    
- 后台docker run指令：
docker run -d --restart always --name incident-management -p 30092:8080 -v /home/app/logs:/apps/logs/ -e spring.profiles.active=prod -e TZ=Asia/Shanghai incident-management:latest

- 前台DockerFile文件：

  ```
  FROM nginx:latest
    
  COPY ./dist/ /usr/share/nginx/html/
    
  EXPOSE 80
  ```

 - 打镜像指令：
     docker build -t incident-management-web:latest .       
    前台启动指令：       
    docker run -d --restart always --incident-management-web -p 30092:8090 -v /home/data/incident-manage-web/config/:/etc/nginx/conf.d - v /home/data/incident-manage-web/ssl/:/etc/nginx/ssl incident-management-web:latest

#### 2、Docker Compose部署

- 制作了基于docker-compse的服务快速安装包，并进行部署

    [root@VM-16-3-centos incident-manage-web]# cd /home/docker-compose-install/
    cp docker-compose /usr/local/bin/
    chmod +x /usr/local/bin/docker-compose
    docker-compose version
    docker-compose up -d
    docker ps |grep incident
    
    [root@VM-16-3-centos docker-compose-install]# ll
    total 11936
    -rw-r--r-- 1 root root 12212176 Mar  9  2022 docker-compose
    -rw-r--r-- 1 root root      755 Dec  1 18:54 docker-compose.yaml
    drwxr-xr-x 2 root root     4096 Dec  1 19:04 images
    [root@VM-16-3-centos docker-compose-install]# docker ps |grep incident
    [root@VM-16-3-centos docker-compose-install]# docker-compose up -d
    Creating network "docker-compose-install_default" with the default driver
    Creating incident-manage-service ... done
    Creating incident-manage-web     ... done
    [root@VM-16-3-centos docker-compose-install]# docker ps |grep incident
    c82b7e837c7f   incident-management-web:latest                      "/docker-entrypoint.…"   4 seconds ago   Up 3 seconds   80/tcp, 0.0.0.0:30092->443/tcp, :::30092->443/tcp        incident-manage-web
    7ed95f5b1a27   incident-manage-service:latest                      "java -jar app.jar"      4 seconds ago   Up 3 seconds   8080/tcp                                                 incident-manage-service


#### 3、K8S helm chart包部署

- 前后端服务进行了应用编排，制作了helm chart安装包，并进行部署

 kubectl create ns incident-system

     [root@node3 incident-manage]# ll
     total 8
     drwxr-xr-x 2 root root    6 Dec  1 20:10 charts
     -rw-r--r-- 1 root root 1151 Nov 29 23:30 Chart.yaml
     drwxr-xr-x 2 root root   80 Dec  1 20:10 templates
     -rw-r--r-- 1 root root  449 Dec  1 20:04 values.yaml
     [root@node3 incident-manage]# helm install incident -n incident-system .
     NAME: incident
     LAST DEPLOYED: Sun Dec  1 20:11:56 2024
     NAMESPACE: incident-system
     STATUS: deployed
     REVISION: 1
     TEST SUITE: None
     [root@node3 incident-manage]# kubectl get svc -n incident-system
     NAME                        TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)         AGE
     incident-manage-service     ClusterIP   10.233.38.137   <none>        8080/TCP        39s
     incident-manage-web-https   NodePort    10.233.56.31    <none>        443:30092/TCP   39s
     [root@node3 incident-manage]# kubectl get configmap -n incident-system
     NAME                       DATA   AGE
     incident-manage-web-conf   1      50s
     kube-root-ca.crt           1      44h
     ssl-crt                    1      50s
     ssl-key                    1      50s
     [root@node3 incident-manage]# kubectl get po -n incident-system
     NAME                                         READY   STATUS    RESTARTS   AGE
     incident-manage-service-65857744dd-h28jn     1/1     Running   0          64s
     incident-manage-web-https-5c4b4c8746-nsp7t   1/1     Running   0          64s

### **安全优化**：

#### 支持https访问:
   - 自制ssl自签名证书，配置在在前台nginx中，保证系统访问安全。

    制作https证书，并保存成configmap.yaml文件：
     openssl req -newkey rsa:2048 -nodes -keyout tls.key -x509 -days 365 -out tls.crt
     kubectl create configmap ssl-key --from-file=tls.key -n incident-system
     kubectl create configmap ssl-crt --from-file=tls.crt -n incident-system
     kubectl get configmap ssl-crt -n incident-system -oyaml >ssl-crt-configmap.yaml
     kubectl get configmap ssl-key -n incident-system -oyaml >ssl-key-configmap.yaml

##  后期扩展方向

    *   数据存数到数据库mysql等数据库中。
    *   使用Redis作为缓存，并处理其分布式系统中的三缓问题及双写一致问题。
    *   基于Spring Cloud Gateway 实现API 网关，方便动态路由、及限流、熔断等操作。
    *   基于Spring Security 安全框架，结合JWT，实现认证、授权、细粒度权限控制等。
    *   集成ELK实现日志采集，方便日志查看及问题定位。
    *   结合Kubernetes 的 Horizontal Pod Autoscaler (HPA) 实现基于指标的自动伸缩，自动调整应用副本数量。

