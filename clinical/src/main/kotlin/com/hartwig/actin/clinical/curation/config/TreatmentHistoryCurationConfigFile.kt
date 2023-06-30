package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.util.ResourceFile
import com.hartwig.actin.util.TabularFile
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.nio.file.Files

object TreatmentHistoryCurationConfigFile {

    private const val DELIMITER = "\t"
    private val LOGGER = LogManager.getLogger(CurationDatabaseReader::class.java)

    @Throws(IOException::class)
    fun read(tsv: String, treatmentDatabase: TreatmentDatabase): List<TreatmentHistoryEntryConfig> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = TabularFile.createFields(lines[0].split(DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray())
        val configs = lines.drop(1)
            .flatMap { line -> repeatPartsForEachTreatmentName(line, fields) }
            .map { (treatmentName, parts) ->
                TreatmentHistoryEntryConfigFactory.createConfig(treatmentName, treatmentDatabase, parts, fields)
            }
        LOGGER.info(" Read ${configs.size} treatment configs from $tsv")
        return configs
    }

    private fun repeatPartsForEachTreatmentName(line: String, fields: Map<String, Int>): List<NamedListOfTsvParts> {
        val parts = line.split(DELIMITER)
        return fields["treatmentName"]?.let { ResourceFile.optionalString(parts[it]) }
            ?.let(CurationUtil::toSet)
            ?.map { NamedListOfTsvParts(it.lowercase(), parts) }
            ?: emptyList()
    }

    private data class NamedListOfTsvParts(val name: String, val parts: List<String>)
}