package com.hartwig.actin.molecular.panel

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.File
import java.io.Reader

val EMPTY_VARIANT_DECOMPOSITION_INDEX = VariantDecompositionIndex(emptyList())

data class VariantDecomposition(
    val originalCodingHgvs: String,
    val decomposedCodingHgvs: List<String>,
)

data class VariantDecompositionIndex(private val entries: List<VariantDecomposition>) {
    init {
        require(entries.all { it.decomposedCodingHgvs.isNotEmpty() }) {
            val invalid = entries.filter { it.decomposedCodingHgvs.isEmpty() }
                .joinToString(", ") { it.originalCodingHgvs }
            "Decomposed coding HGVS list cannot be empty for variant(s): $invalid"
        }
    }

    private val index: Map<String, VariantDecomposition> = entries
        .associateBy { it.originalCodingHgvs }
        .also { idx ->
            require(idx.size == entries.size) {
                val dupes = entries.groupBy { it.originalCodingHgvs }
                    .filter { it.value.size > 1 }
                    .keys.joinToString(", ")
                "Duplicate variant decomposition entries for: $dupes"
            }
        }

    fun lookup(originalCodingHgvs: String): VariantDecomposition? = index[originalCodingHgvs.trim()]
}

private data class RawVariantDecomposition(
    @JsonProperty("variant") val originalCodingHgvs: String,
    @JsonProperty("decomposition") val decomposition: String
)

object PaveVariantDecomposition {
    fun read(reader: Reader): List<VariantDecomposition> {
        val csvReader = CsvMapper().apply {
            enable(CsvParser.Feature.FAIL_ON_MISSING_HEADER_COLUMNS)
            enable(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS)
        }
            .readerFor(RawVariantDecomposition::class.java)
            .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))

        val raw = csvReader.readValues<RawVariantDecomposition>(reader).readAll().toList()
        return raw.map {
            VariantDecomposition(
                originalCodingHgvs = it.originalCodingHgvs.trim(),
                decomposedCodingHgvs = it.decomposition.split(",").map(String::trim).filter { part -> part.isNotEmpty() }
            )
        }
    }

    fun readFromFile(tsvPath: String): List<VariantDecomposition> {
        return File(tsvPath).bufferedReader().use { reader -> read(reader) }
    }
}
