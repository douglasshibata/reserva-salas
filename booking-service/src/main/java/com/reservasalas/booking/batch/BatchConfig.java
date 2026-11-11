package com.reservasalas.booking.batch;

import com.reservasalas.booking.dto.ReservationEvent;
import com.reservasalas.booking.model.DailyConsolidation;
import com.reservasalas.booking.model.Reservation;
import com.reservasalas.booking.model.ReservationStatus;
import com.reservasalas.booking.model.Room;
import com.reservasalas.booking.repository.DailyConsolidationRepository;
import com.reservasalas.booking.repository.ReservationRepository;
import com.reservasalas.booking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import reactor.core.publisher.Sinks;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final DailyConsolidationRepository dailyConsolidationRepository;
    private final Sinks.Many<ReservationEvent> reservationEventSink;

    @Bean
    @StepScope
    public ItemReader<Reservation> expiredReservationsReader() {
        return new ListItemReader<>(
                reservationRepository.findByStatusAndEndTimeBefore(ReservationStatus.CONFIRMED, LocalDateTime.now())
        );
    }

    @Bean
    public ItemProcessor<Reservation, Reservation> expiredReservationsProcessor() {
        return reservation -> {
            reservation.setStatus(ReservationStatus.EXPIRED);
            log.info("Processando expiração da reserva ID: {}", reservation.getId());
            return reservation;
        };
    }

    @Bean
    public ItemWriter<Reservation> expiredReservationsWriter() {
        return items -> {
            for (Reservation reservation : items) {
                reservationRepository.save(reservation);
                Room room = roomRepository.findById(reservation.getRoomId()).orElse(null);
                String roomName = room != null ? room.getName() : "Unknown";

                ReservationEvent event = ReservationEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .reservationId(reservation.getId())
                        .roomId(reservation.getRoomId())
                        .roomName(roomName)
                        .userEmail(reservation.getUserEmail())
                        .startTime(reservation.getStartTime())
                        .endTime(reservation.getEndTime())
                        .eventType("EXPIRED")
                        .timestamp(LocalDateTime.now())
                        .build();
                reservationEventSink.tryEmitNext(event);
            }
        };
    }

    @Bean
    public Step limpezaReservasExpiradasStep() {
        return new StepBuilder("limpezaReservasExpiradasStep", jobRepository)
                .<Reservation, Reservation>chunk(10, transactionManager)
                .reader(expiredReservationsReader())
                .processor(expiredReservationsProcessor())
                .writer(expiredReservationsWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Reservation> remindersReader() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusHours(24);
        return new ListItemReader<>(
                reservationRepository.findByStatusAndReminderSentFalseAndStartTimeBetween(
                        ReservationStatus.CONFIRMED, now, windowEnd
                )
        );
    }

    @Bean
    public ItemProcessor<Reservation, Reservation> remindersProcessor() {
        return reservation -> {
            reservation.setReminderSent(true);
            log.info("Gerando lembrete para reserva ID: {} (Início: {})", reservation.getId(), reservation.getStartTime());
            return reservation;
        };
    }

    @Bean
    public ItemWriter<Reservation> remindersWriter() {
        return items -> {
            for (Reservation reservation : items) {
                reservationRepository.save(reservation);
                Room room = roomRepository.findById(reservation.getRoomId()).orElse(null);
                String roomName = room != null ? room.getName() : "Unknown";

                ReservationEvent event = ReservationEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .reservationId(reservation.getId())
                        .roomId(reservation.getRoomId())
                        .roomName(roomName)
                        .userEmail(reservation.getUserEmail())
                        .startTime(reservation.getStartTime())
                        .endTime(reservation.getEndTime())
                        .eventType("REMINDER")
                        .timestamp(LocalDateTime.now())
                        .build();
                reservationEventSink.tryEmitNext(event);
            }
        };
    }

    @Bean
    public Step geracaoLembretesStep() {
        return new StepBuilder("geracaoLembretesStep", jobRepository)
                .<Reservation, Reservation>chunk(10, transactionManager)
                .reader(remindersReader())
                .processor(remindersProcessor())
                .writer(remindersWriter())
                .build();
    }

    @Bean
    public Tasklet consolidacaoDiariaTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);

            List<Reservation> todayReservations = reservationRepository.findByStartTimeBetween(startOfDay, endOfDay);
            int total = todayReservations.size();
            long expired = todayReservations.stream()
                    .filter(r -> r.getStatus() == ReservationStatus.EXPIRED)
                    .count();

            List<Room> activeRooms = roomRepository.findByActiveTrue();
            double maxPossibleHours = activeRooms.size() * 10.0;
            double totalReservedHours = todayReservations.stream()
                    .mapToDouble(r -> java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes() / 60.0)
                    .sum();

            double occupancyRate = maxPossibleHours > 0 ? (totalReservedHours / maxPossibleHours) * 100.0 : 0.0;

            DailyConsolidation consolidation = dailyConsolidationRepository.findByConsolidationDate(today)
                    .orElse(DailyConsolidation.builder()
                            .consolidationDate(today)
                            .build());

            consolidation.setTotalReservations(total);
            consolidation.setExpiredReservations((int) expired);
            consolidation.setOccupancyRate(Math.min(100.0, Math.round(occupancyRate * 100.0) / 100.0));

            dailyConsolidationRepository.save(consolidation);
            log.info("Consolidação diária concluída para {}: Total={}, Expiradas={}, Ocupação={}%",
                    today, total, expired, consolidation.getOccupancyRate());

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step consolidacaoDiariaStep() {
        return new StepBuilder("consolidacaoDiariaStep", jobRepository)
                .tasklet(consolidacaoDiariaTasklet(), transactionManager)
                .build();
    }

    @Bean(name = "manutencaoDiariaJob")
    public Job manutencaoDiariaJob() {
        return new JobBuilder("ManutençãoDiária", jobRepository)
                .start(limpezaReservasExpiradasStep())
                .next(geracaoLembretesStep())
                .next(consolidacaoDiariaStep())
                .build();
    }
}
