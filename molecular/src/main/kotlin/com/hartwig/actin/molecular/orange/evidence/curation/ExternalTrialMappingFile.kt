package com.hartwig.actin.molecular.orange.evidence.curation

import com.google.common.collect.Lists
import com.hartwig.actin.util.TabularFile
import java.io.File
import java.nio.file.Files

object ExternalTrialMappingFile {

    private const val FIELD_DELIMITER: String = "\t"

    fun read(tsv: String): MutableList<ExternalTrialMapping> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val mappings: MutableList<ExternalTrialMapping> = Lists.newArrayList()
        val fields = TabularFile.createFields(lines[0].split(FIELD_DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        for (line in lines.subList(1, lines.size)) {
            mappings.add(fromLine(line, fields))
        }
        return mappings
    }

    private fun fromLine(line: String, fields: Map<String?, Int?>): ExternalTrialMapping {
        val values: Array<String?> = line.split(FIELD_DELIMITER.toRegex()).toTypedArray()
        return ExternalTrialMapping(
            // TODO (KZ): get rid of the !!'s
            externalTrial = values[fields["externalTrial"]!!]!!,
            actinTrial = values[fields["actinTrial"]!!]!!,
        )
    }
}
