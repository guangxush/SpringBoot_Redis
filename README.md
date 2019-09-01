## 功能需求
通常情况下，Tomcat的Servlet容器会默认将Session保存在内存中。如果是单个服务器实例的应用，将Session保存在服务器内存中是一个常用的选择，但是随着服务器数量的增多，这种方法变得不容易扩展。

![session.png](https://github.com/guangxush/iTechHeart/blob/master/image/Redis/redis1.png)

比如上图中，User1通过负载均衡登录到Server1中，并把Session保存在了Server1中，但是此时User1进行操作2的时候访问到了Server2，但是Server2上面并没有保存User1的session，就会产生重新登录的问题。

目前越来越多的应用采用分布式部署，用于实现高可用性和负载均衡等。那么问题来了，如果将同一个应用部署在多个服务器上通过负载均衡对外提供访问，如何实现Session共享？
实现Session共享的方案很多，其中一种常用的就是使用Tomcat等服务器提供的Session共享功能，将Session的内容统一存储在一个数据库（如MySQL）或缓存（如Redis）中。

这里介绍另一种实现Session共享的方案，不依赖于Servlet容器，而是Web应用代码层面的实现，直接在已有项目基础上加入Spring Session框架来实现Session统一存储在Redis中。

![spring-session.png](https://github.com/guangxush/iTechHeart/blob/master/image/Redis/redis1.png)

正如上图中，我们通过Spring Session将User1进行操作1时产生的Session保存在了Redis中，这样用户在访问Server2的时候就可以从Redis中读取User1 Session并验证，做到了多集群中的Session共享。

如果你的Web应用是基于Spring框架开发的，只需要对现有项目进行少量配置，即可将一个单机版的Web应用改为一个分布式应用，由于不基于Servlet容器，所以可以随意将项目移植到其他容器。

## 准备工作
- 安装部署Redis, 可参考[链接](http://www.runoob.com/redis/redis-install.html)

## 使用方法
### pom.xml中引入Redis相关的依赖(其他自行配置)
```
 <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-data-redis</artifactId>
 </dependency>
 <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-data-redis</artifactId>
 </dependency>
 <dependency>
     <groupId>org.springframework.session</groupId>
     <artifactId>spring-session-data-redis</artifactId>
 </dependency>
 <dependency>
     <groupId>redis.clients</groupId>
     <artifactId>jedis</artifactId>
 </dependency>
```
### Configure设置
application.properties
```
#Redis
spring.redis.host=127.0.0.1
## Redis服务器连接端口
spring.redis.port=6379
## 连接超时时间（毫秒）
spring.redis.timeout=300ms
## Redis服务器连接密码（默认为空）
spring.redis.password=135246
## 连接池中的最大连接数
spring.redis.jedis.pool.max-idle=10
## 连接池中的最大空闲连接
spring.redis.lettuce.pool.max-idle=8
## 连接池中的最大阻塞等待时间
spring.redis.jedis.pool.max-wait=-1ms
## 连接池最大阻塞等待时间（使用负值表示没有限制）
spring.redis.lettuce.shutdown-timeout=100ms
spring.session.store-type=redis
server.servlet.session.timeout=2000s
spring.session.redis.flush-mode=on_save
spring.session.redis.namespace=spring:session
```
### 设置Redis配置（这一步仅用于配置缓存，跟Spring Session没有关系，可以不配置）
```java
@Configuration
@EnableCaching
@PropertySource(value = "classpath:/application.properties")
public class RedisConfig extends CachingConfigurerSupport {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    /**
     * 配置连接工厂
     * @return
     */
    @Bean(name = "factory")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(redisPassword));
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    /**
     * 配置缓存管理器
     * @param factory 连接工厂
     * @return
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        return new RedisCacheManager(RedisCacheWriter.nonLockingRedisCacheWriter(factory),
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)).disableCachingNullValues());
    }

    /**
     * Redis操作模板
     * @param factory 连接工厂
     * @return
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setKeySerializer(jackson2JsonRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
```
### 使用spring-session-data-redis实现session共享，pom中引入该依赖（上文已添加），添加SessionConfig配置类。

```java
@Configuration
@EnableRedisHttpSession
public class SessionConfiguration {

}
```

### 编写一个Controller并进行测试
```java
@RequestMapping("testSessionTimeOut")
public String testSessionTimeOut(Long id, HttpSession session, Model model){
    User user = userService.getUserById(id);
    System.out.println("sessionId-------->"+session.getId());
    model.addAttribute("user", JSON.toJSONString(user));
    session.setAttribute("user",JSON.toJSONString(user));
    return "hello world";
}
```

## 最终效果
- 浏览器中输入http://localhost/session/testSessionTimeOut(可在多台机器上部署测试，这里只测试Session是否保存在了Redis中)
- 控制台输出结果

![result1.png](https://github.com/guangxush/iTechHeart/blob/master/image/Redis/redis3.png)

- redis后台查看结果
![result2.png](https://github.com/guangxush/iTechHeart/blob/master/image/Redis/redis4.png)

## 参考文档
[Spring官方文档](https://docs.spring.io/spring-session/docs/current/reference/html5/guides/boot-redis.html#boot-sample)

## Github代码
[Github代码参考](https://github.com/guangxush/SpringBoot_Redis)
