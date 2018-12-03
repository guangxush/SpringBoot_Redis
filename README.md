# SpringBoot_Redis
SpringBoot+Redis
(使用Redis存储HttpSession)<br>
[官方文档](https://docs.spring.io/spring-session/docs/current/reference/html5/guides/boot-redis.html#boot-sample)

### 需求

通常情况下，Tomcat、Jetty等Servlet容器，会默认将Session保存在内存中。如果是单个服务器实例的应用，将Session保存在服务器内存中是一个非常好的方案。但是这种方案有一个缺点，就是不利于扩展。

目前越来越多的应用采用分布式部署，用于实现高可用性和负载均衡等。那么问题来了，如果将同一个应用部署在多个服务器上通过负载均衡对外提供访问，如何实现Session共享？

实际上实现Session共享的方案很多，其中一种常用的就是使用Tomcat、Jetty等服务器提供的Session共享功能，将Session的内容统一存储在一个数据库（如MySQL）或缓存（如Redis）中。

本文主要介绍另一种实现Session共享的方案，不依赖于Servlet容器，而是Web应用代码层面的实现，直接在已有项目基础上加入Spring Session框架来实现Session统一存储在Redis中。如果你的Web应用是基于Spring框架开发的，只需要对现有项目进行少量配置，即可将一个单机版的Web应用改为一个分布式应用，由于不基于Servlet容器，所以可以随意将项目移植到其他容器。

### 准备工作
- 安装部署Redis, 请参考[链接](http://www.runoob.com/redis/redis-install.html)

### pom.xml引入相关的依赖

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
### 设置Redis配置
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

### 测试结果
- 控制台输出结果<br>
![控制台输出结果](https://github.com/guangxush/SpringBoot_Redis/blob/master/image/result1.png)
- redis后台查看结果<br>
![Redis查看结果](https://github.com/guangxush/SpringBoot_Redis/blob/master/image/result2.png)