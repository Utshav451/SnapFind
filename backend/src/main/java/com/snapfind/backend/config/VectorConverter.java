package com.snapfind.backend.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class VectorConverter implements AttributeConverter<float[], String> {

    //Convert float[] → String for storing in DB
    //Format: [0.1,0.2,0.3,...]
    @Override
    public String convertToDatabaseColumn(float[] embedding) {
        if (embedding == null) return null;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    //Convert String → float[] when reading from DB
    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String cleaned = dbData.replace("[", "").replace("]", "");
        String[] parts = cleaned.split(",");
        float[] embedding = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            embedding[i] = Float.parseFloat(parts[i].trim());
        }
        return embedding;
    }
}
