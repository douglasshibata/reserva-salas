package com.reservasalas.booking.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job manutencaoDiariaJob;

    @Scheduled(cron = "${batch.job.cron:0 0 1 * * ?}")
    public void scheduleDailyMaintenance() {
        log.info("Iniciando execução agendada do Job ManutençãoDiária...");
        runManutencaoJob();
    }

    public JobExecution runManutencaoJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(manutencaoDiariaJob, params);
            log.info("Job ManutençãoDiária finalizado com status: {}", execution.getStatus());
            return execution;
        } catch (Exception e) {
            log.error("Erro ao executar Job ManutençãoDiária", e);
            throw new RuntimeException("Falha na execução do Batch Job: " + e.getMessage(), e);
        }
    }
}
