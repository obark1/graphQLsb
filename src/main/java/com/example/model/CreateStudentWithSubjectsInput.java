package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStudentWithSubjectsInput {
    private String firstName;
    private String lastName;
    private String email;
    private String street;
    private String city;
    private List<Subject> subjects;
}
