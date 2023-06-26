package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.util.ResourceFile
import com.hartwig.actin.util.TabularFile
import java.io.File
import java.io.IOException
import java.nio.file.Files

object TreatmentHistoryCurationConfigFile {

    private const val DELIMITER = "\t"

    @Throws(IOException::class)
    fun read(tsv: String, treatmentsByName: Map<String, Treatment>): List<TreatmentHistoryEntryConfig> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = TabularFile.createFields(lines[0].split(DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray())
        return lines.drop(1)
            .flatMap { line -> repeatPartsForEachTreatmentName(line, fields) }
            .map { (treatmentName, parts) ->
                TreatmentHistoryEntryConfigFactory.createConfig(treatmentName, treatmentsByName, parts, fields)
            }
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