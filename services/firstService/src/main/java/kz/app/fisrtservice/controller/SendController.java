package kz.app.fisrtservice.controller;

import kz.app.commondto.dto.AppealRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/send")
@RequiredArgsConstructor
public class SendController {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/appeal")
    public String sendTestAppeal() {
        AppealRequestDTO appeal = new AppealRequestDTO();
        appeal.setEventId(UUID.randomUUID().toString());
        appeal.setClientId(12345L);
        appeal.setCategory("SUPPORT");
        appeal.setTopic("Technical issue");
        appeal.setDescription("Cannot login to account");
        appeal.setCreatedAt(LocalDateTime.now());

        kafkaTemplate.send("topic-1", appeal.getEventId(), appeal);

        return "Test appeal sent: " + appeal.getEventId();
    }

    @PostMapping("/bulk")
    public String sendBulkAppeals(@RequestParam int count) {
        for (int i = 0; i < count; i++) {
            AppealRequestDTO appeal = new AppealRequestDTO();
            appeal.setEventId(UUID.randomUUID().toString());
            appeal.setClientId(10000L + i);
            appeal.setCategory(i % 2 == 0 ? "SUPPORT" : "COMPLAINT");
            appeal.setTopic("Issue " + i);
            appeal.setDescription("Description for issue " + i);
            appeal.setCreatedAt(LocalDateTime.now());

            kafkaTemplate.send("topic-1", appeal.getEventId(), appeal);
        }
        return "Sent " + count + " appeals";
    }
}
