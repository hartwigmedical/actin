package com.hartwig.actin.molecular.panel

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.File
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Reader

// TODO for dev, move to resource and load properly
val DEFAULT_VARIANT_DECOMPOSITION = VariantDecompositionIndex(
    PaveVariantDecomposition.readFromFile("/tmp/variant_decomposition.tsv")
)

data class VariantDecomposition(
    val proteinHgvs: String,
    val decomposedCodingHgvs: List<String>,
)

data class VariantDecompositionIndex(private val entries: List<VariantDecomposition>) {
    private val index: Map<String, VariantDecomposition> = entries
        .associateBy { it.proteinHgvs }
        .also { idx ->
            require(idx.size == entries.size) {
                val dupes = entries.groupBy { it.proteinHgvs }
                    .filter { it.value.size > 1 }
                    .keys.joinToString(", ")
                "Duplicate variant decomposition entries for: $dupes"
            }
        }

    fun lookup(proteinHgvs: String): VariantDecomposition? = index[proteinHgvs.trim()]
}

private data class RawVariantDecomposition(
    @JsonProperty("variant") val variant: String,
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
                proteinHgvs = it.variant.trim(),
                decomposedCodingHgvs = it.decomposition.split(",").map(String::trim).filter { part -> part.isNotEmpty() }
            )
        }
    }

    fun readFromFile(tsvPath: String): List<VariantDecomposition> {
        return File(tsvPath).bufferedReader().use { reader -> read(reader) }
    }
}