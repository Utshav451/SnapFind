package com.snapfind.backend.service;

import com.snapfind.backend.config.AuthUtil;
import com.snapfind.backend.dto.PhotoResponse;
import com.snapfind.backend.entity.Collection;
import com.snapfind.backend.entity.Photo;
import com.snapfind.backend.exception.ForbiddenException;
import com.snapfind.backend.exception.ResourceNotFoundException;
import com.snapfind.backend.repository.CollectionRepository;
import com.snapfind.backend.repository.GuestCollectionRepository;
import com.snapfind.backend.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final CollectionRepository collectionRepository;
    private final AuthUtil authUtil;
    private final FaceService faceService;
    private final GuestCollectionRepository guestCollectionRepository;
    private final S3Service s3Service;

    // PREVIOUS LOCAL STORAGE CONFIG:
    // @Value("${app.upload-dir}")
    // private String uploadDir;

    //Manual constructor with @Lazy on FaceService
    public PhotoService(PhotoRepository photoRepository,
                        CollectionRepository collectionRepository,
                        AuthUtil authUtil,
                        GuestCollectionRepository guestCollectionRepository,
                        S3Service s3Service,
                        @Lazy FaceService faceService) {
        this.photoRepository = photoRepository;
        this.collectionRepository = collectionRepository;
        this.authUtil = authUtil;
        this.guestCollectionRepository = guestCollectionRepository;
        this.s3Service = s3Service;
        this.faceService = faceService;
    }

    //Upload photos to a collection
    public List<PhotoResponse> uploadPhotos(Long collectionId,
                                            List<MultipartFile> files)
            throws IOException {

        var currentUser = authUtil.getCurrentUser();

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        //New — owner OR any guest who has accessed the collection
        boolean isOwner = collection.getOwner().getId().equals(currentUser.getId());
        boolean isGuest = guestCollectionRepository.existsByUserAndCollection(currentUser, collection);

        if (!isOwner && !isGuest) {
            throw new ForbiddenException("You do not have access to this collection");
        }

        /*
        //PREVIOUS LOCAL STORAGE IMPLEMENTATION:
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        return files.stream().map(file -> {
            try {
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                Files.copy(file.getInputStream(), filePath);

                Photo photo = new Photo();
                photo.setCollection(collection);
                photo.setFilePath(filePath.toString());
                photo.setOriginalName(file.getOriginalFilename());
                Photo savedPhoto = photoRepository.save(photo);

                faceService.extractAndSaveEmbedding(savedPhoto);

                return toResponse(savedPhoto);

            } catch (IOException e) {
                throw new RuntimeException("Failed to upload: "
                        + file.getOriginalFilename());
            }
        }).collect(Collectors.toList());
        */

        //AWS S3 STORAGE IMPLEMENTATION:
        return files.stream().map(file -> {
            try {
                String filename = "photos/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
                String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

                //Upload directly to AWS S3 bucket
                s3Service.uploadFile(filename, file.getInputStream(), file.getSize(), contentType);

                Photo photo = new Photo();
                photo.setCollection(collection);
                photo.setFilePath(filename); //Store S3 object key
                photo.setOriginalName(file.getOriginalFilename());
                Photo savedPhoto = photoRepository.save(photo);

                //Background face recognition extraction from S3
                faceService.extractAndSaveEmbedding(savedPhoto);

                return toResponse(savedPhoto);

            } catch (IOException e) {
                throw new RuntimeException("Failed to upload to S3: "
                        + file.getOriginalFilename(), e);
            }
        }).collect(Collectors.toList());
    }

    //Get all photos in a collection
    public List<PhotoResponse> getPhotos(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
        return photoRepository.findByCollection(collection)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    //Delete a single photo
    public void deletePhoto(Long photoId) {
        var currentUser = authUtil.getCurrentUser();

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));

        if (!photo.getCollection().getOwner().getId()
                .equals(currentUser.getId())) {
            throw new ForbiddenException("You are not the owner of this photo");
        }

        /*
        // PREVIOUS LOCAL STORAGE DELETE:
        try {
            Files.deleteIfExists(Paths.get(photo.getFilePath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete photo file");
        }
        */

        //AWS S3 DELETE:
        s3Service.deleteFile(photo.getFilePath());

        photoRepository.delete(photo);
    }

    /*
    // PREVIOUS LOCAL FILE PATH RETRIEVAL:
    public Path getPhotoFilePath(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));
        return Paths.get(photo.getFilePath());
    }
    */

    //AWS S3 PHOTO RETRIEVAL
    public Photo getPhotoById(Long photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));
    }

    public byte[] getPhotoBytes(String key) throws IOException {
        return s3Service.downloadFileBytes(key);
    }

    //Convert Photo to PhotoResponse DTO
    public PhotoResponse toResponse(Photo photo) {
        return new PhotoResponse(
                photo.getId(),
                photo.getOriginalName(),
                "/api/photos/" + photo.getId() + "/file",
                photo.getUploadedAt()
        );
    }
}