package com.hartwig.actin.molecular.orange.evidence.curation

import com.hartwig.actin.util.TabularFile
import java.io.File
import java.nio.file.Files

object ExternalTrialMappingFile {

    private const val FIELD_DELIMITER: String = "\t"

    fun read(tsv: String): List<ExternalTrialMapping> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = TabularFile.createFields(lines[0].split(FIELD_DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        return lines.subList(1, lines.size).map { fromLine(it, fields) }
    }

    private fun fromLine(line: String, fields: Map<String, Int>): ExternalTrialMapping {
        val values = line.split(FIELD_DELIMITER.toRegex())
        return ExternalTrialMapping(
            externalTrial = values[fields["externalTrial"]!!],
            actinTrial = values[fields["actinTrial"]!!],
        )
    }
}
