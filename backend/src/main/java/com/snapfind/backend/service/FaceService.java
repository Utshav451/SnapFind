package com.snapfind.backend.service;

import com.snapfind.backend.dto.PhotoResponse;
import com.snapfind.backend.dto.SearchResponse;
import com.snapfind.backend.entity.FaceEmbedding;
import com.snapfind.backend.entity.Photo;
import com.snapfind.backend.exception.BadRequestException;
import com.snapfind.backend.repository.FaceEmbeddingRepository;
import com.snapfind.backend.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaceService {

    private final PhotoRepository photoRepository;
    private final FaceEmbeddingRepository faceEmbeddingRepository;
    private final RestTemplate restTemplate;
    private final S3Service s3Service;

    @Value("${app.python-service-url}")
    private String pythonServiceUrl;

    @Value("${app.face-similarity-threshold}")
    private double threshold;

    // PREVIOUS LOCAL UPLOAD DIR:
    // @Value("${app.upload-dir}")
    // private String uploadDir;

    //Extract all face embeddings from photo and save
    @Async
    public void extractAndSaveEmbedding(Photo photo) {
        try {
            /*
            //PREVIOUS LOCAL FILE EXTRACTION:
            List<float[]> embeddings =
                    callPythonExtract(photo.getFilePath());
            */

            //AWS S3 FILE EXTRACTION
            byte[] photoBytes = s3Service.downloadFileBytes(photo.getFilePath());
            List<float[]> embeddings = callPythonExtract(photoBytes, photo.getOriginalName());

            if (embeddings != null && !embeddings.isEmpty()) {
                for (float[] embedding : embeddings) {
                    FaceEmbedding faceEmbedding = new FaceEmbedding();
                    faceEmbedding.setPhoto(photo);
                    //Store as string "[0.1,0.2,...]"
                    faceEmbedding.setEmbedding(toVectorString(embedding));
                    faceEmbeddingRepository.save(faceEmbedding);
                }
                System.out.println("Saved " + embeddings.size()
                        + " face(s) for photo " + photo.getId());
            } else {
                System.out.println("No faces detected in photo "
                        + photo.getId());
            }
        } catch (Exception e) {
            System.out.println("Embedding extraction failed for photo "
                    + photo.getId() + ": " + e.getMessage());
        }
    }

    //Search photos by selfie
    public SearchResponse searchBySelfie(Long collectionId,
                                         MultipartFile selfie)
            throws IOException {

        System.out.println("=== FACE SEARCH STARTED ===");
        System.out.println("Collection ID: " + collectionId);

        /*
        // PREVIOUS LOCAL TEMP FILE CODE
        String tempFilename = "temp_" + UUID.randomUUID() + "_"
                + selfie.getOriginalFilename();
        Path tempPath = Paths.get(uploadDir, tempFilename);
        Files.createDirectories(tempPath.getParent());
        Files.copy(selfie.getInputStream(), tempPath);
        System.out.println("Temp file saved: " + tempPath);

        try {
            System.out.println("Calling Python extract...");
            List<float[]> selfieEmbeddings =
                    callPythonExtract(tempPath.toString());
        ...
        } finally {
            Files.deleteIfExists(tempPath);
        }
        */

        // IN-MEMORY SELFIE PROCESSING (NO DISK WRITE):
        try {
            System.out.println("Calling Python extract with selfie in-memory bytes...");
            byte[] selfieBytes = selfie.getBytes();
            List<float[]> selfieEmbeddings =
                    callPythonExtract(selfieBytes, selfie.getOriginalFilename());

            System.out.println("Python response received");
            System.out.println("Embeddings null? " + (selfieEmbeddings == null));
            if (selfieEmbeddings != null) {
                System.out.println("Embeddings count: " + selfieEmbeddings.size());
            }

            if (selfieEmbeddings == null || selfieEmbeddings.isEmpty()) {
                throw new BadRequestException(
                        "No face detected in selfie. " +
                                "Please upload a clear front-facing photo.");
            }

            float[] selfieEmbedding = selfieEmbeddings.get(0);
            String embeddingStr = toVectorString(selfieEmbedding);
            System.out.println("Embedding string length: "
                    + embeddingStr.length());
            System.out.println("Threshold: " + threshold);

            System.out.println("Searching face_embeddings table...");
            List<Long> matchingPhotoIds = faceEmbeddingRepository
                    .findMatchingPhotoIds(
                            collectionId, embeddingStr, threshold);

            System.out.println("Matching photo IDs: " + matchingPhotoIds);

            if (matchingPhotoIds.isEmpty()) {
                return new SearchResponse(0, List.of());
            }

            List<PhotoResponse> matchedPhotos = matchingPhotoIds.stream()
                    .map(id -> photoRepository.findById(id)
                            .map(this::toResponse)
                            .orElse(null))
                    .filter(p -> p != null)
                    .collect(Collectors.toList());

            System.out.println("Matched photos count: "
                    + matchedPhotos.size());
            System.out.println("=== FACE SEARCH COMPLETED ===");

            return new SearchResponse(matchedPhotos.size(), matchedPhotos);

        } catch (Exception e) {
            System.out.println("=== FACE SEARCH ERROR ===");
            System.out.println("Error type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /*
    // PREVIOUS LOCAL FILE PYTHON EXTRACT
    //Call Python /extract —> returns list of embeddings
    private List<float[]> callPythonExtract(String filePath) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(filePath));

            HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                pythonServiceUrl + "/extract", request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null) {

                List<List<Double>> embeddingsList =
                    (List<List<Double>>) response.getBody()
                        .get("embeddings");

                if (embeddingsList != null && !embeddingsList.isEmpty()) {
                    return embeddingsList.stream().map(embList -> {
                        float[] embedding = new float[embList.size()];
                        for (int i = 0; i < embList.size(); i++) {
                            embedding[i] = embList.get(i).floatValue();
                        }
                        return embedding;
                    }).collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            System.out.println("Python service call failed: "
                + e.getMessage());
        }
        return null;
    }
    */

    //IN-MEMORY BYTE[] PYTHON EXTRACT (S3 & SELFIE):
    private List<float[]> callPythonExtract(byte[] imageBytes, String filename) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            org.springframework.core.io.ByteArrayResource resource =
                    new org.springframework.core.io.ByteArrayResource(imageBytes) {
                        @Override
                        public String getFilename() {
                            return filename != null ? filename : "image.jpg";
                        }
                    };
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                pythonServiceUrl + "/extract", request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null) {

                List<List<Double>> embeddingsList =
                    (List<List<Double>>) response.getBody()
                        .get("embeddings");

                if (embeddingsList != null && !embeddingsList.isEmpty()) {
                    return embeddingsList.stream().map(embList -> {
                        float[] embedding = new float[embList.size()];
                        for (int i = 0; i < embList.size(); i++) {
                            embedding[i] = embList.get(i).floatValue();
                        }
                        return embedding;
                    }).collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            System.out.println("Python service call failed: "
                + e.getMessage());
        }
        return null;
    }

    //Convert float[] to pgvector format [0.1,0.2,...]
    private String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    //Convert Photo to PhotoResponse DTO
    private PhotoResponse toResponse(Photo photo) {
        return new PhotoResponse(
            photo.getId(),
            photo.getOriginalName(),
            "/api/photos/" + photo.getId() + "/file",
            photo.getUploadedAt()
        );
    }
}