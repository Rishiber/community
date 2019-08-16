package com.nowcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Title: ThreadPoolConfig
 * @Description:
 * @Author: Rishiber
 * @Version: 1.0
 * @create: 2019/8/15 20:18
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
