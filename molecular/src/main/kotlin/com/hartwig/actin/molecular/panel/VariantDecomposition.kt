package com.hartwig.actin.molecular.panel

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.io.Reader

val EMPTY_VARIANT_DECOMPOSITION_TABLE = VariantDecompositionTable(emptyList())

data class VariantDecomposition(
    val gene: String,
    val transcript: String?,
    val originalCodingHgvs: String,
    val decomposedCodingHgvs: List<String>,
)

class VariantDecompositionTable(private val entries: List<VariantDecomposition>) {
    init {
        require(entries.all { it.gene.isNotBlank() }) {
            val invalid = entries.filter { it.gene.isBlank() }
                .joinToString(", ") { it.originalCodingHgvs }
            "Gene cannot be empty for variant decomposition entries: $invalid"
        }
        require(entries.all { it.decomposedCodingHgvs.isNotEmpty() }) {
            val invalid = entries.filter { it.decomposedCodingHgvs.isEmpty() }
                .joinToString(", ") { it.originalCodingHgvs }
            "Decomposed coding HGVS list cannot be empty for variant(s): $invalid"
        }
    }

    private val index: Map<VariantDecompositionKey, VariantDecomposition> = entries
        .associateBy { keyFor(it.gene, it.transcript, it.originalCodingHgvs) }
        .also { idx ->
            require(idx.size == entries.size) {
                val dupes = entries.groupBy { keyFor(it.gene, it.transcript, it.originalCodingHgvs) }
                    .filter { it.value.size > 1 }
                    .keys.joinToString(", ") { key ->
                        listOfNotNull(key.gene, key.transcript, key.originalCodingHgvs).joinToString(":")
                    }
                "Duplicate variant decomposition entries for: $dupes"
            }
        }

    fun lookup(gene: String, transcript: String?, originalCodingHgvs: String): VariantDecomposition? =
        index[keyFor(gene, transcript, originalCodingHgvs)]
}

private data class RawVariantDecomposition(
    val gene: String,
    val transcript: String,
    val variant: String,
    val decomposition: String
)

object PaveVariantDecomposition {
    fun read(reader: Reader): List<VariantDecomposition> {
        val csvReader = CsvMapper().apply {
            registerModule(KotlinModule.Builder().build())
            enable(CsvParser.Feature.FAIL_ON_MISSING_HEADER_COLUMNS)
            enable(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS)
        }
            .readerFor(RawVariantDecomposition::class.java)
            .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))

        val raw = csvReader.readValues<RawVariantDecomposition>(reader).readAll().toList()
        return raw.map {
            val gene = it.gene.trim()
            require(gene.isNotEmpty()) {
                "Gene cannot be empty for variant ${it.variant.trim()}"
            }
            VariantDecomposition(
                gene = gene,
                transcript = it.transcript.trim().ifEmpty { null },
                originalCodingHgvs = it.variant.trim(),
                decomposedCodingHgvs = it.decomposition.split(",").map(String::trim).filter { part -> part.isNotEmpty() }
            )
        }
    }

    fun readFromFile(tsvPath: String): List<VariantDecomposition> {
        return File(tsvPath).bufferedReader().use { reader -> read(reader) }
    }
}

private data class VariantDecompositionKey(
    val gene: String,
    val transcript: String?,
    val originalCodingHgvs: String
)

private fun keyFor(gene: String, transcript: String?, originalCodingHgvs: String): VariantDecompositionKey {
    val normalizedTranscript = transcript?.trim()?.ifEmpty { null }
    return VariantDecompositionKey(gene.trim(), normalizedTranscript, originalCodingHgvs.trim())
}
