package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StudentConnection {
    List<StudentEdge> edges;
    PageInfo pageInfo;
    Integer totalCount;

    // Computed from edges
    public List<Student> getNodes() {
        return edges.stream()
                .map(StudentEdge::getNode)
                .toList();
    }
}
