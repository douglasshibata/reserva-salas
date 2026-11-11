package com.reservasalas.booking.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/batch/jobs")
@RequiredArgsConstructor
public class BatchJobController {

    private final BatchJobScheduler batchJobScheduler;

    @PostMapping("/manutencao")
    public ResponseEntity<Map<String, Object>> executeManutencaoDiaria() {
        JobExecution execution = batchJobScheduler.runManutencaoJob();
        return ResponseEntity.ok(Map.of(
                "jobName", execution.getJobInstance().getJobName(),
                "executionId", execution.getId(),
                "status", execution.getStatus().toString(),
                "startTime", execution.getStartTime() != null ? execution.getStartTime().toString() : "",
                "endTime", execution.getEndTime() != null ? execution.getEndTime().toString() : ""
        ));
    }
}
