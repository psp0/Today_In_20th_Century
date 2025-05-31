package place.run.mep.century20.config;

import org.springframework.http.HttpStatus;

public record TokenValidationResult(boolean isValid, String message, HttpStatus status) {}
