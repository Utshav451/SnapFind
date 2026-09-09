package com.snapfind.backend.controller;

import com.snapfind.backend.dto.PhotoResponse;
import com.snapfind.backend.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    //POST /api/collections/{id}/photos
    //Photographer uploads photos to a collection
    @PostMapping("/collections/{id}/photos")
    public ResponseEntity<List<PhotoResponse>> uploadPhotos(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files)
            throws IOException {
        return ResponseEntity.ok(photoService.uploadPhotos(id, files));
    }

    //GET /api/collections/{id}/photos
    //Get all photos in a collection
    @GetMapping("/collections/{id}/photos")
    public ResponseEntity<List<PhotoResponse>> getPhotos(
            @PathVariable Long id) {
        return ResponseEntity.ok(photoService.getPhotos(id));
    }

    //DELETE /api/photos/{photoId}
    //Photographer deletes a single photo
    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable Long photoId) {
        photoService.deletePhoto(photoId);
        return ResponseEntity.noContent().build();
    }

    //GET /api/photos/{photoId}/file
    //Serve the actual image file for display or download
    @GetMapping("/photos/{photoId}/file")
    public ResponseEntity<Resource> getPhotoFile(
            @PathVariable Long photoId)
            throws MalformedURLException {

        Path filePath = photoService.getPhotoFilePath(photoId);
        Resource resource = new UrlResource(filePath.toUri());

        //Detect content type from file extension
        String contentType = "image/jpeg";
        if (filePath.toString().toLowerCase().endsWith(".png")) {
            contentType = "image/png";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\""
                                + filePath.getFileName().toString() + "\"")
                .body(resource);
    }
}