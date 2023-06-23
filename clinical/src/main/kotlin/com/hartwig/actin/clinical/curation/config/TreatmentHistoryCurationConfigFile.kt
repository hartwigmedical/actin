package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.util.ResourceFile
import com.hartwig.actin.util.TabularFile
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.nio.file.Files

object TreatmentHistoryCurationConfigFile {
    private val LOGGER = LogManager.getLogger(TreatmentHistoryCurationConfigFile::class.java)

    private const val DELIMITER = "\t"

    @Throws(IOException::class)
    fun read(tsv: String, treatmentsByName: Map<String, Treatment>): List<TreatmentHistoryEntryConfig> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = TabularFile.createFields(lines[0].split(DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray())
        val (configs, inputs) = lines.drop(1)
            .flatMap { line -> repeatPartsForEachTreatmentName(line, fields) }
            .map { (treatmentName, parts) ->
                TreatmentHistoryEntryConfigFactory.createConfig(treatmentName, treatmentsByName, parts, fields)
            }
            .reduce { acc, result -> acc + result }

        (treatmentsByName.keys - inputs).forEach { LOGGER.warn("Treatment with name '$it' not used in resolving prior treatments") }
        return configs
    }

    private fun repeatPartsForEachTreatmentName(line: String, fields: Map<String, Int>): List<NamedTsvParts> {
        val parts = line.split(DELIMITER)
        return fields["treatmentName"]?.let { ResourceFile.optionalString(parts[it]) }
            ?.let(CurationUtil::toSet)
            ?.map { NamedTsvParts(it.lowercase(), parts) }
            ?: emptyList()
    }

    private data class NamedTsvParts(val name: String, val parts: List<String>)
}