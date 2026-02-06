package com.example.model;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class StudentConnection {
    List<StudentEdge> edges;
    PageInfo pageInfo;
    Integer totalCount;
}
