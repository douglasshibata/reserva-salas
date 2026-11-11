package com.reservasalas.booking;

import com.reservasalas.booking.batch.BatchJobScheduler;
import com.reservasalas.booking.model.DailyConsolidation;
import com.reservasalas.booking.repository.DailyConsolidationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BatchJobTest {

    @Autowired
    private BatchJobScheduler batchJobScheduler;

    @Autowired
    private DailyConsolidationRepository dailyConsolidationRepository;

    @Test
    void testExecuteBatchManutencaoDiariaJob() {
        JobExecution execution = batchJobScheduler.runManutencaoJob();

        assertThat(execution).isNotNull();
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Optional<DailyConsolidation> consolidation = dailyConsolidationRepository.findByConsolidationDate(LocalDate.now());
        assertThat(consolidation).isPresent();
        assertThat(consolidation.get().getTotalReservations()).isGreaterThanOrEqualTo(0);
    }
}
