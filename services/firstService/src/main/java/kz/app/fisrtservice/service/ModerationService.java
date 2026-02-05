package kz.app.fisrtservice.service;

import kz.app.commondto.dto.AppealRequestDTO;
import kz.app.commondto.dto.AppealResponseDTO;
import kz.app.commondto.dto.ModerationResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModerationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppealEnrichmentService enrichmentService;
    private final ModerationRulesService rulesService;

    @KafkaListener(topics = "topic-1", groupId = "first-service-group")
    public void consumeAppeal(AppealRequestDTO appeal) {
        log.info("Received appeal for moderation: {}", appeal.getEventId());

        try {
            // 1. Проверка идемпотентности (чтобы eventId не обрабатывался дважды)
            if (rulesService.isAlreadyProcessed(appeal.getEventId())) {
                log.info("Appeal {} already processed, skipping", appeal.getEventId());
                return;
            }

            // 2. Обогащение данных через Service-2
            var enrichedAppeal = enrichmentService.enrichAppeal(appeal);

            // 3. Применение бизнес-правил
            boolean isApproved = rulesService.applyModerationRules(enrichedAppeal);

            // 4. Если обращение прошло все правила - публикуем в Topic-2
            if (isApproved) {
                ModerationResultDTO result = createModerationResult(enrichedAppeal, true, "Approved");
                kafkaTemplate.send("topic-2", appeal.getEventId(), result);
                log.info("Appeal {} approved and sent to topic-2", appeal.getEventId());
            } else {
                log.info("Appeal {} rejected by moderation rules", appeal.getEventId());
            }

            // 5. Помечаем eventId как обработанный (для идемпотентности)
            rulesService.markAsProcessed(appeal.getEventId());

        } catch (Exception e) {
            log.error("Error processing appeal {}", appeal.getEventId(), e);
        }
    }

    private ModerationResultDTO createModerationResult(AppealResponseDTO appeal, boolean approved, String reason) {
        ModerationResultDTO result = new ModerationResultDTO();
        result.setEventId(appeal.getEventId());
        result.setClientId(appeal.getClientId());
        result.setCategory(appeal.getCategory());
        result.setTopic(appeal.getTopic());
        result.setApproved(approved);
        result.setReason(reason);
        result.setModeratedAt(java.time.LocalDateTime.now());
        return result;
    }
}