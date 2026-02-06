package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StudentPage {
    private List<Student> students;
    private boolean hasNextPage;
    private int count;


    // Getters
}
