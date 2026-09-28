package com.example.rag

import com.example.storage.dao.RagDao
import com.example.storage.entity.RagChunkEntity
import com.example.storage.entity.RagDocumentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class RagSearchResult(
    val chunkId: Long,
    val docId: Long,
    val content: String,
    val score: Float
)

class LocalRagEngine(private val ragDao: RagDao) {

    val allDocuments: Flow<List<RagDocumentEntity>> = ragDao.getAllDocuments()

    suspend fun importDocument(title: String, fileType: String, content: String): Long = withContext(Dispatchers.IO) {
        val chunks = chunkText(content)
        val doc = RagDocumentEntity(
            title = title,
            fileType = fileType,
            rawContent = content,
            chunkCount = chunks.size
        )
        val docId = ragDao.insertDocument(doc)

        val chunkEntities = chunks.mapIndexed { index, chunkText ->
            val keywords = extractKeywords(chunkText)
            RagChunkEntity(
                docId = docId,
                chunkIndex = index,
                content = chunkText,
                keywords = keywords
            )
        }
        ragDao.insertChunks(chunkEntities)
        docId
    }

    suspend fun deleteDocument(docId: Long) = withContext(Dispatchers.IO) {
        ragDao.deleteChunksForDoc(docId)
        ragDao.deleteDocument(docId)
    }

    suspend fun searchRelevantChunks(query: String, topK: Int = 3): List<RagSearchResult> = withContext(Dispatchers.IO) {
        val queryTerms = query.lowercase().split(Regex("[^a-zA-Z0-9_-]+")).filter { it.length > 2 }
        if (queryTerms.isEmpty()) return@withContext emptyList()

        val allChunks = ragDao.getAllChunks()
        val scored = mutableListOf<RagSearchResult>()

        for (chunk in allChunks) {
            val chunkLower = chunk.content.lowercase()
            var score = 0f
            for (term in queryTerms) {
                if (chunkLower.contains(term)) {
                    score += 1.0f
                }
                if (chunk.keywords.contains(term)) {
                    score += 1.5f
                }
            }
            if (score > 0f) {
                scored.add(
                    RagSearchResult(
                        chunkId = chunk.id,
                        docId = chunk.docId,
                        content = chunk.content,
                        score = score
                    )
                )
            }
        }

        scored.sortedByDescending { it.score }.take(topK)
    }

    fun buildRagContext(searchResults: List<RagSearchResult>): String {
        if (searchResults.isEmpty()) return ""
        return buildString {
            appendLine("--- LOCAL PROJECT KNOWLEDGE BASE (OFFLINE RETRIEVAL) ---")
            searchResults.forEachIndexed { idx, res ->
                appendLine("[Doc Chunk ${idx + 1} | Match Score: ${"%.1f".format(res.score)}]")
                appendLine(res.content.trim())
                appendLine()
            }
            appendLine("--- END LOCAL KNOWLEDGE BASE ---")
        }
    }

    private fun chunkText(text: String, chunkSize: Int = 400): List<String> {
        val paragraphs = text.split("\n\n")
        val chunks = mutableListOf<String>()
        var currentChunk = StringBuilder()

        for (p in paragraphs) {
            if (currentChunk.length + p.length > chunkSize && currentChunk.isNotEmpty()) {
                chunks.add(currentChunk.toString().trim())
                currentChunk = StringBuilder()
            }
            if (currentChunk.isNotEmpty()) currentChunk.append("\n\n")
            currentChunk.append(p)
        }

        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString().trim())
        }

        return chunks.ifEmpty { listOf(text) }
    }

    private fun extractKeywords(text: String): String {
        val stopWords = setOf("the", "and", "for", "with", "this", "that", "from", "are", "have", "not")
        return text.lowercase()
            .split(Regex("[^a-zA-Z0-9_-]+"))
            .filter { it.length > 3 && !stopWords.contains(it) }
            .distinct()
            .take(15)
            .joinToString(",")
    }
}
