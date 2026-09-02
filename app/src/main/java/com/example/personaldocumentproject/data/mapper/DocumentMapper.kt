package com.example.personaldocumentproject.data.mapper

import com.example.personaldocumentproject.data.local.DocumentEntity
import com.example.personaldocumentproject.data.local.converters.DocumentConverters
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentCategory

class DocumentMapper {
    fun toDomain(entity: DocumentEntity) = Document(
        id = entity.id, imageUri = entity.imageUri, title = entity.title,
        description = entity.description, ocrText = entity.ocrText,
        category = runCatching { DocumentCategory.valueOf(entity.category) }.getOrDefault(DocumentCategory.OTHER),
        tags = DocumentConverters.toStringList(entity.tags),
        detectedObjects = DocumentConverters.toStringList(entity.detectedObjects),
        embedding = DocumentConverters.toFloatList(entity.embedding),
        createdAt = entity.createdAt,
    )

    fun toEntity(document: Document) = DocumentEntity(
        id = document.id, imageUri = document.imageUri, title = document.title,
        description = document.description, ocrText = document.ocrText,
        category = document.category.name,
        tags = DocumentConverters.fromStringList(document.tags),
        detectedObjects = DocumentConverters.fromStringList(document.detectedObjects),
        embedding = DocumentConverters.fromFloatList(document.embedding),
        createdAt = document.createdAt,
    )
}
