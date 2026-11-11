package com.reservasalas.booking.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    @Bean
    public Counter reservasCriadasCounter(MeterRegistry registry) {
        return Counter.builder("reservas.criadas")
                .description("Contador de reservas confirmadas criadas com sucesso")
                .register(registry);
    }

    @Bean
    public Counter reservasConflitosCounter(MeterRegistry registry) {
        return Counter.builder("reservas.conflitos")
                .description("Contador de tentativas de reserva com conflito de horário")
                .register(registry);
    }

    @Bean
    public Counter redisHitsCounter(MeterRegistry registry) {
        return Counter.builder("redis.cache.hits")
                .description("Contador de acertos no cache Redis")
                .register(registry);
    }

    @Bean
    public Counter redisMissesCounter(MeterRegistry registry) {
        return Counter.builder("redis.cache.misses")
                .description("Contador de falhas (misses) no cache Redis")
                .register(registry);
    }

    @Bean
    public Timer endpointLatencyTimer(MeterRegistry registry) {
        return Timer.builder("reservas.latencia.endpoint")
                .description("Timer de medição de latência dos endpoints reativos")
                .register(registry);
    }
}
