package com.snapfind.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CollectionResponse {
    private Long id;
    private String name;
    private String uniqueKey;
    private String ownerName;
    private LocalDateTime createdAt;
}