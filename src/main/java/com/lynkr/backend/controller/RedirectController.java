package com.lynkr.backend.controller;

import com.lynkr.backend.exception.BadRequestException;
import com.lynkr.backend.exception.ResourceNotFoundException;
import com.lynkr.backend.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortCode:[a-zA-Z0-9_-]+}")
    public void redirectToOriginalUrl(@PathVariable String shortCode,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws IOException {
        try {
            String originalUrl = urlService.handleRedirectAndRecordAnalytics(shortCode, request);
            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            response.setHeader("Location", originalUrl);
        } catch (ResourceNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/html");
            response.getWriter().write("<html><body><h2>404 - Short Link Not Found</h2><p>" + e.getMessage() + "</p></body></html>");
        } catch (BadRequestException e) {
            response.setStatus(HttpServletResponse.SC_GONE);
            response.setContentType("text/html");
            response.getWriter().write("<html><body><h2>410 - Link Expired</h2><p>" + e.getMessage() + "</p></body></html>");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/html");
            response.getWriter().write("<html><body><h2>500 - Server Error</h2><p>" + e.getMessage() + "</p></body></html>");
        }
    }
}
