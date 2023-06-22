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
        val (configs, searchedNames) = lines.drop(1)
            .flatMap { line ->
                val parts = line.split(DELIMITER)
                fields["treatmentName"]?.let { ResourceFile.optionalString(parts[it]) }
                    ?.let(CurationUtil::toSet)
                    ?.map { Pair(it.lowercase(), parts) }
                    ?: emptyList()
            }
            .map { (treatmentName, parts) ->
                TreatmentHistoryEntryConfigFactory.createConfig(treatmentName, treatmentsByName, parts, fields)
            }
            .reduce { acc, pair -> Pair(acc.first + pair.first, acc.second + pair.second) }

        (treatmentsByName.keys - searchedNames).forEach { LOGGER.warn("Treatment with name '$it' not used in resolving prior treatments") }
        return configs
    }
}