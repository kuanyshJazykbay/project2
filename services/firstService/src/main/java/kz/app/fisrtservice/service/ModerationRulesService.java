package kz.app.fisrtservice.service;

import kz.app.commondto.dto.AppealResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ModerationRulesService {

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @Value("${moderation.work-start:09:00}")
    private String workStart;

    @Value("${moderation.work-end:18:00}")
    private String workEnd;

    @Value("${moderation.categories-off-hours:URGENT,COMPLAINT}")
    private String offHoursCategoriesStr;

    private Set<String> offHoursCategories;

    public boolean isAlreadyProcessed(String eventId) {
        return processedEventIds.contains(eventId);
    }

    public void markAsProcessed(String eventId) {
        processedEventIds.add(eventId);
    }

    public boolean applyModerationRules(AppealResponseDTO appeal) {
        log.info("Applying moderation rules for appeal {}", appeal.getEventId());

        // Правило 1: Проверка активных обращений
        if (appeal.isHasActiveAppeals()) {
            log.info("Appeal {} rejected: client has active appeals", appeal.getEventId());
            return false;
        }

        // Правило 2: Проверка рабочего времени
        if (isOffHoursCategory(appeal.getCategory()) && !isWithinWorkingHours()) {
            log.info("Appeal {} rejected: outside working hours for category {}",
                    appeal.getEventId(), appeal.getCategory());
            return false;
        }

        // Правило 3: Дополнительные проверки
        if ("BLOCKED".equals(appeal.getStatus())) {
            log.info("Appeal {} rejected: client is blocked", appeal.getEventId());
            return false;
        }

        log.info("Appeal {} passed all moderation rules", appeal.getEventId());
        return true;
    }

    private boolean isOffHoursCategory(String category) {
        if (category == null) return false;
        if (offHoursCategories == null) {
            offHoursCategories = new HashSet<>(Arrays.asList(offHoursCategoriesStr.split(",")));
        }
        return offHoursCategories.contains(category.toUpperCase());
    }

    private boolean isWithinWorkingHours() {
        try {
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.parse(workStart);
            LocalTime end = LocalTime.parse(workEnd);
            return !now.isBefore(start) && !now.isAfter(end);
        } catch (Exception e) {
            log.error("Error checking working hours. Using workStart={}, workEnd={}", workStart, workEnd);
            return true;
        }
    }
}