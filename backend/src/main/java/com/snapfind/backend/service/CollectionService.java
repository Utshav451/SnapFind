package com.snapfind.backend.service;

import com.snapfind.backend.config.AuthUtil;
import com.snapfind.backend.dto.CollectionRequest;
import com.snapfind.backend.dto.CollectionResponse;
import com.snapfind.backend.dto.AccessRequest;
import com.snapfind.backend.entity.Collection;
import com.snapfind.backend.entity.GuestCollection;
import com.snapfind.backend.entity.User;
import com.snapfind.backend.exception.ForbiddenException;
import com.snapfind.backend.exception.ResourceNotFoundException;
import com.snapfind.backend.repository.CollectionRepository;
import com.snapfind.backend.repository.GuestCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final GuestCollectionRepository guestCollectionRepository;
    private final AuthUtil authUtil;

    //Photographer: create collection
    public CollectionResponse createCollection(CollectionRequest request) {
        User currentUser = authUtil.getCurrentUser();

        Collection collection = new Collection();
        collection.setName(request.getName());
        collection.setUniqueKey(generateUniqueKey());
        collection.setOwner(currentUser);

        Collection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    //Photographer: get all my collections
    public List<CollectionResponse> getMyCollections() {
        User currentUser = authUtil.getCurrentUser();
        return collectionRepository.findByOwner(currentUser)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    //Both: get single collection by ID
    public CollectionResponse getCollectionById(Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
        return toResponse(collection);
    }

    //Photographer: delete entire collection
    public void deleteCollection(Long id) {
        User currentUser = authUtil.getCurrentUser();

        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        //Only owner can delete
        if (!collection.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not the owner of this collection");
        }

        //Cascade handles deleting photos and guest_collections
        collectionRepository.delete(collection);
    }

    //Guest: access collection via invite key
    public CollectionResponse accessCollection(AccessRequest request) {
        User currentUser = authUtil.getCurrentUser();

        //Find collection by key
        Collection collection = collectionRepository
                .findByUniqueKey(request.getKey())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Collection not found or has been deleted"));


        //Save to guest list only if not already saved
        if (!guestCollectionRepository.existsByUserAndCollection(
                currentUser, collection)) {
            GuestCollection guestCollection = new GuestCollection();
            guestCollection.setUser(currentUser);
            guestCollection.setCollection(collection);
            guestCollectionRepository.save(guestCollection);
        }

        return toResponse(collection);
    }

    //Guest: get saved collections
    public List<CollectionResponse> getSavedCollections() {
        User currentUser = authUtil.getCurrentUser();
        return guestCollectionRepository.findByUser(currentUser)
                .stream()
                .map(gc -> toResponse(gc.getCollection()))
                .collect(Collectors.toList());
    }

    //Guest: remove collection from saved list
    public void removeSavedCollection(Long collectionId) {
        User currentUser = authUtil.getCurrentUser();

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        GuestCollection guestCollection = guestCollectionRepository
                .findByUserAndCollection(currentUser, collection)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Collection not in your saved list"));
        guestCollectionRepository.delete(guestCollection);
    }

    //Ggenerate human-readable key
    private String generateUniqueKey() {
        String[] words = {
                "SUNSET", "WINTER", "SPRING", "GOLDEN", "SILVER",
                "GARDEN", "BREEZE", "MYSTIC", "JOYFUL", "SERENE",
                "TENDER", "RADIANT", "LOVELY", "DREAMY", "FESTIVE"
        };
        String word = words[new Random().nextInt(words.length)];
        int number = 1000 + new Random().nextInt(9000);
        String key = word + "-" + number;

        //Make sure key is unique
        if (collectionRepository.findByUniqueKey(key).isPresent()) {
            return generateUniqueKey();
        }
        return key;
    }

    //Convert Collection entity to CollectionResponse DTO
    private CollectionResponse toResponse(Collection collection) {
        return new CollectionResponse(
                collection.getId(),
                collection.getName(),
                collection.getUniqueKey(),
                collection.getOwner().getName(),
                collection.getCreatedAt()
        );
    }
}
