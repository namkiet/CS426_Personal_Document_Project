package com.example.personaldocumentproject.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    indices = [Index(value = ["createdAt"])],
)
data class DocumentEntity(
    @PrimaryKey val id: String,
    val imageUri: String,
    val title: String,
    val description: String,
    val ocrText: String,
    val category: String,
    val tags: String,
    val detectedObjects: String,
    val embedding: String,
    val createdAt: Long,
)
