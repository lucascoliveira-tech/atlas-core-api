package br.com.lucascoliveira.atlas.core.status;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/status")
class StatusController {

    @GetMapping
    ResponseEntity<StatusResponse> status() {
        return ResponseEntity.ok(new StatusResponse("atlas-core-api", "UP", Instant.now()));
    }

    record StatusResponse(String service, String status, Instant timestamp) {
    }
}

