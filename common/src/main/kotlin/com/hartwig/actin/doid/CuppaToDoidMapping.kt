package com.hartwig.actin.doid

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.apache.logging.log4j.LogManager
import java.io.File

private data class CuppaDoidRow(
    @param:JsonProperty("cuppa_cancer_type") val cuppaCancerType: String = "",
    @param:JsonProperty("doid_ids") val doidIds: String = "",
    @param:JsonProperty("doid_labels") val doidLabels: String? = null,
    @param:JsonProperty("notes") val notes: String? = null
)

class CuppaToDoidMapping(private val mapping: Map<String, Set<String>>) {

    fun doidsForCuppaType(cancerType: String): Set<String>? {
        return mapping[cancerType]
    }

    companion object {
        private val logger = LogManager.getLogger(CuppaToDoidMapping::class.java)
        private const val DOID_SEPARATOR = ";"
        private const val DOID_PREFIX = "DOID:"

        fun createFromFile(tsvPath: String): CuppaToDoidMapping {
            logger.info("Loading CUPPA to DOID mapping from {}", tsvPath)
            val reader = CsvMapper().apply {
                enable(CsvParser.Feature.EMPTY_STRING_AS_NULL)
            }.readerFor(CuppaDoidRow::class.java).with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))

            val rows = reader.readValues<CuppaDoidRow>(File(tsvPath)).readAll()
            val mapping = rows.associate { row ->
                row.cuppaCancerType to row.doidIds.split(DOID_SEPARATOR).map { it.removePrefix(DOID_PREFIX) }.toSet()
            }
            logger.info(" Loaded {} CUPPA cancer type mappings", mapping.size)
            return CuppaToDoidMapping(mapping)
        }
    }
}
