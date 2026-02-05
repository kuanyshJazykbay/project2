package kz.app.fisrtservice.service;

import kz.app.commondto.dto.AppealRequestDTO;
import kz.app.commondto.dto.AppealResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppealEnrichmentService {

    private final RestTemplate restTemplate;

    @Value("${spring.rest.service2.url}")
    private String service2Url;

    @Retryable(
            maxAttemptsExpression = "${spring.retry.service2.maxAttempts}",
            backoff = @Backoff(delayExpression = "${spring.retry.service2.backoffDelay}")
    )
    public AppealResponseDTO enrichAppeal(AppealRequestDTO appeal) {
        log.info("Enriching appeal {} via Service-2", appeal.getEventId());

        try {
            ResponseEntity<AppealResponseDTO> response = restTemplate.postForEntity(
                    service2Url + "/enrich",
                    appeal,
                    AppealResponseDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                // Если Service-2 вернул ошибку, создаем минимальный ответ
                log.warn("Service-2 returned error for appeal {}, using fallback", appeal.getEventId());
                return createFallbackResponse(appeal);
            }

        } catch (Exception e) {
            log.error("Error calling Service-2 for appeal {}", appeal.getEventId(), e);
            // При падении Service-2 возвращаем fallback-ответ
            return createFallbackResponse(appeal);
        }
    }

    private AppealResponseDTO createFallbackResponse(AppealRequestDTO appeal) {
        AppealResponseDTO fallback = new AppealResponseDTO();
        fallback.setEventId(appeal.getEventId());
        fallback.setClientId(appeal.getClientId());
        fallback.setCategory(appeal.getCategory());
        fallback.setTopic(appeal.getTopic());
        fallback.setDescription(appeal.getDescription());
        fallback.setCreatedAt(appeal.getCreatedAt());
        fallback.setHasActiveAppeals(false); // По умолчанию нет активных обращений
        fallback.setStatus("ENRICHMENT_FAILED");
        return fallback;
    }
}