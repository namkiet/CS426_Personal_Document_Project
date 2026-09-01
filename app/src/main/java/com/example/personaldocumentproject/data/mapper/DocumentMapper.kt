package com.example.personaldocumentproject.data.mapper

import com.example.personaldocumentproject.data.local.DocumentEntity
import com.example.personaldocumentproject.data.local.converters.DocumentConverters
import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentCategory

class DocumentMapper {
    fun toDomain(entity: DocumentEntity) = Document(
        id = entity.id, imageUri = entity.imageUri, title = entity.title,
        description = entity.description, extractedText = entity.extractedText,
        category = runCatching { DocumentCategory.valueOf(entity.category) }.getOrDefault(DocumentCategory.OTHER),
        tags = DocumentConverters.toStringList(entity.tags),
        embedding = DocumentConverters.toFloatList(entity.embedding),
        createdAtEpochMillis = entity.createdAtEpochMillis,
    )

    fun toEntity(document: Document) = DocumentEntity(
        id = document.id, imageUri = document.imageUri, title = document.title,
        description = document.description, extractedText = document.extractedText,
        category = document.category.name,
        tags = DocumentConverters.fromStringList(document.tags),
        embedding = DocumentConverters.fromFloatList(document.embedding),
        createdAtEpochMillis = document.createdAtEpochMillis,
    )
}
