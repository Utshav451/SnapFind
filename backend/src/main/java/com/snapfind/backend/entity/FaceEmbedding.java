package com.snapfind.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "face_embeddings")
public class FaceEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "photo_id", nullable = false)
    private Photo photo;

    //Storing as text, cast to vector manually in queries
    @Column(name = "embedding", columnDefinition = "TEXT")
    private String embedding;
}
