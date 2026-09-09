package com.snapfind.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PhotoResponse {
    private Long id;
    private String originalName;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}