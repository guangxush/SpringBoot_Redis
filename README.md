# SpringBoot_Redis
SpringBoot+Redis
(使用Redis存储HttpSession)<br>
[官方文档](https://docs.spring.io/spring-session/docs/current/reference/html5/guides/boot-redis.html#boot-sample)

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
- 控制台输出结果
[控制台输出结果](https://github.com/guangxush/SpringBoot_Redis/blob/master/image/result1.png)
- redis后台查看结果
[Redis查看结果](https://github.com/guangxush/SpringBoot_Redis/blob/master/image/result2.png)