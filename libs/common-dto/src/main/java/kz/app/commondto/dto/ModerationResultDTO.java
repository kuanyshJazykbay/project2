package kz.app.commondto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModerationResultDTO {
    private String eventId;
    private Long clientId;
    private String category;
    private String topic;
    private boolean approved;
    private String reason;
    private LocalDateTime moderatedAt;
}
