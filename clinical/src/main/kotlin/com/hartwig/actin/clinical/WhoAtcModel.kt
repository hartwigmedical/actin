package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.datamodel.AtcClassification
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.ImmutableAtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import com.hartwig.actin.util.TabularFile
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files
import java.util.*

interface AtcModel {
    fun resolve(rawAtcCode: String): AtcClassification?
}

private const val ATC_LENGTH_4_LEVELS = 5

class WhoAtcModel(private val atcMap: Map<String, String>) : AtcModel {

    override fun resolve(rawAtcCode: String): AtcClassification? {
        return if (rawAtcCode.trim().isNotEmpty() && rawAtcCode[0].lowercaseChar() in 'a'..'z') {
            return if (rawAtcCode.length >= ATC_LENGTH_4_LEVELS) {
                ImmutableAtcClassification.builder()
                    .anatomicalMainGroup(atcLevel(rawAtcCode.substring(0, 1)))
                    .therapeuticSubGroup(atcLevel(rawAtcCode.substring(0, 3)))
                    .pharmacologicalSubGroup(atcLevel(rawAtcCode.substring(0, 4)))
                    .chemicalSubGroup(atcLevel(rawAtcCode.substring(0, 5)))
                    .chemicalSubstance(maybeAtcLevel(if (rawAtcCode.length > ATC_LENGTH_4_LEVELS) rawAtcCode.substring(0, 7) else null))
                    .build()
            } else {
                LOGGER.warn("ATC code $rawAtcCode did not contain at least 4 levels of classification. Ignoring ATC code for this medication")
                null
            }
        } else {
            null
        }
    }

    private fun maybeAtcLevel(levelCode: String?): Optional<AtcLevel> =
        levelCode?.let { Optional.of(atcLevel(it)) } ?: Optional.empty()

    private fun atcLevel(levelCode: String): ImmutableAtcLevel =
        ImmutableAtcLevel.builder().code(levelCode).name(lookup(levelCode)).build()

    private fun lookup(level: String) =
        atcMap[level] ?: throw IllegalArgumentException("ATC code [$level] not found in tree")

    companion object {
        private val LOGGER = LogManager.getLogger(CurationDatabaseReader::class.java)
        fun createFromFile(tsvPath: String): WhoAtcModel {
            val lines = Files.readAllLines(File(tsvPath).toPath())
            val fields = TabularFile.createFields(lines[0].split(TabularFile.DELIMITER).dropLastWhile { it.isEmpty() }
                .toTypedArray())
            return WhoAtcModel(lines.map { it.split(TabularFile.DELIMITER).toTypedArray() }
                .associate { line -> line[fields["ATC code"]!!] to line[fields["ATC level name"]!!] })
        }
    }
}