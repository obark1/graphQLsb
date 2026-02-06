package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PageInfo {
    Boolean hasNextPage;
    Boolean hasPrevPage;
    String startCursor;
    String endCursor;
}
