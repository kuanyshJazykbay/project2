package kz.app.secondservice.controller;

import kz.app.commondto.dto.AppealRequestDTO;
import kz.app.commondto.dto.AppealResponseDTO;
import kz.app.secondservice.service.AppealEnrichmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appeals")
@RequiredArgsConstructor
public class AppealController {
    private final AppealEnrichmentService enrichmentService;

    @PostMapping("/enrich")
    public ResponseEntity<AppealResponseDTO> enrichAppeal(@RequestBody AppealRequestDTO request) {
        AppealResponseDTO response = enrichmentService.enrichAppealData(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service-2 is UP");
    }
}
