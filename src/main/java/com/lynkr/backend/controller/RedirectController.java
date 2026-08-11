package com.lynkr.backend.controller;

import com.lynkr.backend.exception.BadRequestException;
import com.lynkr.backend.exception.ResourceNotFoundException;
import com.lynkr.backend.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortCode:[a-zA-Z0-9_-]+}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortCode,
                                                  HttpServletRequest request) {
        try {
            String originalUrl = urlService.handleRedirectAndRecordAnalytics(shortCode, request);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(originalUrl))
                    .build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("<html><body><h2>404 - Short Link Not Found</h2><p>" + e.getMessage() + "</p></body></html>");
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body("<html><body><h2>410 - Link Expired</h2><p>" + e.getMessage() + "</p></body></html>");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<html><body><h2>500 - Server Error</h2><p>" + e.getMessage() + "</p></body></html>");
        }
    }
}
