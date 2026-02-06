package com.example.controller;

import com.example.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class JacksonController {
    @GetMapping("/user/public")
    @JsonView(Views.Public.class)
    public Employee getPublicUser() {
        return new Employee(5L, "James", LocalDateTime.now());
    }

    @GetMapping("/user/internal")
    @JsonView(Views.Internal.class)
    public Employee getInternalUser() {
        return new Employee(5L, "James", LocalDateTime.now());
    }

    @PostMapping("/user")
    public ResponseEntity<String> createUser(@RequestBody Employee employee) {
        return ResponseEntity.ok("Received: " + employee.getFirstName());
    }

}
