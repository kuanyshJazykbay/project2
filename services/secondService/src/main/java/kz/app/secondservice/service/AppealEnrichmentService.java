package kz.app.secondservice.service;

import kz.app.commondto.dto.AppealRequestDTO;
import kz.app.commondto.dto.AppealResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppealEnrichmentService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Вместо @Cacheable просто сохраняем в Redis
    public AppealResponseDTO enrichAppealData(AppealRequestDTO request) {
        log.info("Enriching appeal data for eventId: {}", request.getEventId());

        // Проверяем, есть ли уже в Redis
        AppealResponseDTO cached = getFromRedis(request.getEventId());
        if (cached != null) {
            log.info("Returning cached data for eventId: {}", request.getEventId());
            return cached;
        }

        // Если нет в кэше - создаем новые данные
        AppealResponseDTO response = createEnrichedResponse(request);

        // Сохраняем в Redis
        saveToRedis(request.getEventId(), response);

        return response;
    }

    private AppealResponseDTO createEnrichedResponse(AppealRequestDTO request) {
        AppealResponseDTO response = new AppealResponseDTO();
        response.setEventId(request.getEventId());
        response.setClientId(request.getClientId());
        response.setCategory(request.getCategory());
        response.setTopic(request.getTopic());
        response.setDescription(request.getDescription());
        response.setCreatedAt(request.getCreatedAt());

        // Имитация данных
        boolean hasActiveAppeals = new Random().nextInt(100) < 20;
        response.setHasActiveAppeals(hasActiveAppeals);

        List<String> tags = Arrays.asList("VIP", "LOYAL", "NEW");
        response.setClientTags(Arrays.asList(tags.get(new Random().nextInt(tags.size()))));

        response.setPriority(new Random().nextInt(5) + 1);
        response.setStatus("ENRICHED");

        return response;
    }

    private void saveToRedis(String eventId, AppealResponseDTO response) {
        try {
            String key = "appeal:" + eventId;
            redisTemplate.opsForValue().set(key, response, 1, TimeUnit.HOURS); // TTL 1 час
            log.info("Saved to Redis: {}", key);
        } catch (Exception e) {
            log.error("Failed to save to Redis: {}", eventId, e);
        }
    }

    private AppealResponseDTO getFromRedis(String eventId) {
        try {
            String key = "appeal:" + eventId;
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof AppealResponseDTO) {
                return (AppealResponseDTO) value;
            }
        } catch (Exception e) {
            log.error("Failed to get from Redis: {}", eventId, e);
        }
        return null;
    }
}