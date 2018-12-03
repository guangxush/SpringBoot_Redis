package com.shgx.redis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Session配置
 *
 * @author guangxush
 */

@Configuration
@EnableRedisHttpSession
public class SessionConfiguration {

}
