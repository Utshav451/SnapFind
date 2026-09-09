package com.snapfind.backend.repository;

import com.snapfind.backend.entity.FaceEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FaceEmbeddingRepository
        extends JpaRepository<FaceEmbedding, Long> {

    @Query(value = """
        SELECT DISTINCT f.photo_id
        FROM face_embeddings f
        JOIN photos p ON f.photo_id = p.id
        WHERE p.collection_id = :collectionId
        AND 1 - (CAST(f.embedding AS vector) <=> CAST(:embedding AS vector)) > :threshold
        """, nativeQuery = true)
    List<Long> findMatchingPhotoIds(
            @Param("collectionId") Long collectionId,
            @Param("embedding") String embedding,
            @Param("threshold") double threshold
    );
}