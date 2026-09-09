package com.snapfind.backend.controller;

import com.snapfind.backend.dto.SearchResponse;
import com.snapfind.backend.service.FaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FaceController {

    private final FaceService faceService;

    //POST /api/collections/{id}/search
    //Guest uploads selfie to find their photos in a collection
    @PostMapping("/collections/{id}/search")
    public ResponseEntity<SearchResponse> searchBySelfie(
            @PathVariable Long id,
            @RequestParam("selfie") MultipartFile selfie)
            throws IOException {
        return ResponseEntity.ok(
                faceService.searchBySelfie(id, selfie));
    }
}