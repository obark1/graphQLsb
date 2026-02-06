package com.example.controller;

import com.example.util.LocalDateTimeDeserializerCustom;
import com.example.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    private Long id;
    @JsonView(Views.Public.class)
    private String firstName;
    @JsonView(Views.Internal.class)
    @JsonDeserialize(using = LocalDateTimeDeserializerCustom.class)
    private LocalDateTime dateOfBirth;
}
