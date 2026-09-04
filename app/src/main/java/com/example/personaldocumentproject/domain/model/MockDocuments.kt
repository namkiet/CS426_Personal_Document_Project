package com.example.personaldocumentproject.domain.model

object MockDocuments {
    val items = listOf(
        Document(
            id = "mock-database-notes",
            imageUri = "",
            title = "Database Indexing Notes",
            description = "Lecture notes covering B-trees, clustered indexes, and query optimization strategies.",
            ocrText = "DATABASE SYSTEMS\nChapter 7: Indexing\nB+ Tree indexes keep records sorted and support efficient range queries.",
            category = DocumentCategory.STUDY,
            tags = listOf("database", "lecture", "indexing"),
            detectedObjects = listOf("Notebook", "Laptop", "Pen"),
            embedding = emptyList(),
            createdAt = 1_788_451_200_000,
        ),
        Document(
            id = "mock-grocery-receipt",
            imageUri = "",
            title = "Weekend Grocery Receipt",
            description = "Groceries and household supplies purchased for the weekend.",
            ocrText = "FRESH MARKET\nMilk 3.49\nCoffee 8.90\nFruit 12.45\nTOTAL 42.18",
            category = DocumentCategory.RECEIPT,
            tags = listOf("groceries", "expense", "weekend"),
            detectedObjects = listOf("Receipt", "Printed text"),
            embedding = emptyList(),
            createdAt = 1_788_278_400_000,
        ),
        Document(
            id = "mock-project-ideas",
            imageUri = "",
            title = "Project Ideas",
            description = "Early ideas and action items for the personal document application.",
            ocrText = "PROJECT IDEAS\n- Fast document capture\n- Local OCR\n- Search by meaning\n- Private by default",
            category = DocumentCategory.NOTE,
            tags = listOf("ideas", "planning"),
            detectedObjects = listOf("Sticky note", "Handwriting"),
            embedding = emptyList(),
            createdAt = 1_786_982_400_000,
        ),
        Document(
            id = "mock-algorithms",
            imageUri = "",
            title = "Algorithms Cheat Sheet",
            description = "A quick reference for common graph traversal algorithms and complexity.",
            ocrText = "BFS: O(V + E)\nDFS: O(V + E)\nDijkstra: O((V + E) log V)",
            category = DocumentCategory.STUDY,
            tags = listOf("algorithms", "exam"),
            detectedObjects = listOf("Paper", "Diagram"),
            embedding = emptyList(),
            createdAt = 1_785_340_800_000,
        ),
    )
}
