package com.snapfind.backend.controller;

import com.snapfind.backend.dto.AccessRequest;
import com.snapfind.backend.dto.CollectionRequest;
import com.snapfind.backend.dto.CollectionResponse;
import com.snapfind.backend.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    //POST /api/collections
    //Photographer creates a new collection
    @PostMapping
    public ResponseEntity<CollectionResponse> createCollection(
            @RequestBody CollectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionService.createCollection(request));
    }

    //GET /api/collections/mine
    //Photographer gets all their collections
    @GetMapping("/mine")
    public ResponseEntity<List<CollectionResponse>> getMyCollections() {
        return ResponseEntity.ok(collectionService.getMyCollections());
    }

    //GET /api/collections/{id}
    //Get a single collection by ID
    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponse> getCollectionById(
            @PathVariable Long id) {
        return ResponseEntity.ok(collectionService.getCollectionById(id));
    }

    //DELETE /api/collections/{id}
    //Photographer deletes entire collection
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }

    //POST /api/collections/access
    //Guest pastes invite key to access collection
    @PostMapping("/access")
    public ResponseEntity<CollectionResponse> accessCollection(
            @RequestBody AccessRequest request) {
        return ResponseEntity.ok(collectionService.accessCollection(request));
    }

    //GET /api/collections/saved
    //Guest gets their saved collections list
    @GetMapping("/saved")
    public ResponseEntity<List<CollectionResponse>> getSavedCollections() {
        return ResponseEntity.ok(collectionService.getSavedCollections());
    }

    //DELETE /api/collections/saved/{id}
    //Guest removes collection from their saved list only
    @DeleteMapping("/saved/{id}")
    public ResponseEntity<Void> removeSavedCollection(
            @PathVariable Long id) {
        collectionService.removeSavedCollection(id);
        return ResponseEntity.noContent().build();
    }
}