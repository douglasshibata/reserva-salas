package com.reservasalas.booking.config;

import com.reservasalas.booking.dto.ReservationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "jpaTaskExecutor")
    public Executor jpaTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("jpa-reactive-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Scheduler jpaScheduler(Executor jpaTaskExecutor) {
        return Schedulers.fromExecutor(jpaTaskExecutor);
    }

    @Bean
    public Sinks.Many<ReservationEvent> reservationEventSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}
