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
    @param:JsonProperty("excluded_doid_ids") val excludedDoidIds: String? = null,
    @param:JsonProperty("doid_labels") val doidLabels: String? = null,
    @param:JsonProperty("notes") val notes: String? = null
)

data class CuppaDoids(
    val included: Set<String>,
    val excluded: Set<String>? = null
)

class CuppaToDoidMapping(private val mapping: Map<String, CuppaDoids>) {

    fun doidsForCuppaType(cancerType: String): CuppaDoids? {
        return mapping[cancerType]
    }

    companion object {
        private val logger = LogManager.getLogger(CuppaToDoidMapping::class.java)
        private const val DOID_SEPARATOR = ";"

        fun createFromFile(tsvPath: String): CuppaToDoidMapping {
            logger.info("Loading CUPPA to DOID mapping from {}", tsvPath)
            val reader = CsvMapper().apply {
                enable(CsvParser.Feature.EMPTY_STRING_AS_NULL)
            }.readerFor(CuppaDoidRow::class.java).with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))

            val rows = reader.readValues<CuppaDoidRow>(File(tsvPath)).readAll()
            val mapping = rows.associate { row ->
                row.cuppaCancerType to CuppaDoids(
                    included = row.doidIds.split(DOID_SEPARATOR).toSet(),
                    excluded = row.excludedDoidIds?.split(DOID_SEPARATOR)?.toSet()
                )
            }
            logger.info(" Loaded {} CUPPA cancer type mappings", mapping.size)
            return CuppaToDoidMapping(mapping)
        }
    }
}
