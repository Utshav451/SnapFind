package com.snapfind.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse {
    private int totalMatches;
    private List<PhotoResponse> matchedPhotos;
}
