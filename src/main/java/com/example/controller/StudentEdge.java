package com.example.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class StudentEdge {
    com.example.controller.Student node;
    String cursor;
}
