package com.blitz.order.infrastructure.outbox;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 打开定时任务，供 {@link OrderEventRelay} 的 outbox 扫描使用。
 *
 * <p>单独放一个配置类而不是加到启动类上：minimal 模式没有 Kafka，
 * 也就没必要开一个空转的调度线程池。
 */
@Configuration
@Profile("!minimal")
@EnableScheduling
public class OrderOutboxConfig {
}
