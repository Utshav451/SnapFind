package com.snapfind.backend.repository;

import com.snapfind.backend.entity.Collection;
import com.snapfind.backend.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    //Get all photos in a collection
    List<Photo> findByCollection(Collection collection);

    //Delete all photos of a collection
    void deleteByCollection(Collection collection);

    //Vector similarity search — find photos where face matches selfie
    //within a specific collection, ordered by closest match
    @Query(value = """
        SELECT * FROM photos
        WHERE collection_id = :collectionId
        AND embedding IS NOT NULL
        AND 1 - (embedding <=> CAST(:embedding AS vector)) > :threshold
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT 50
        """, nativeQuery = true)
    List<Photo> findMatchingFaces(
            @Param("collectionId") Long collectionId,
            @Param("embedding") String embedding,
            @Param("threshold") double threshold
    );
}