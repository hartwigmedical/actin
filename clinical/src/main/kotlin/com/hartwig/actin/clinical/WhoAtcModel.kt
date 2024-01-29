package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.datamodel.AtcClassification
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.util.TabularFile
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files

interface AtcModel {
    fun resolveByCode(rawAtcCode: String): AtcClassification?
    fun resolveByName(name: String): Set<String>
}

private const val ATC_LENGTH_4_LEVELS = 5

class WhoAtcModel(private val atcMap: Map<String, String>) : AtcModel {

    override fun resolveByCode(rawAtcCode: String): AtcClassification? {
        return if (rawAtcCode.trim().isNotEmpty() && rawAtcCode[0].lowercaseChar() in 'a'..'z') {
            return if (rawAtcCode.length >= ATC_LENGTH_4_LEVELS) {
                AtcClassification(
                    anatomicalMainGroup = atcLevel(rawAtcCode.substring(0, 1)),
                    therapeuticSubGroup = atcLevel(rawAtcCode.substring(0, 3)),
                    pharmacologicalSubGroup = atcLevel(rawAtcCode.substring(0, 4)),
                    chemicalSubGroup = atcLevel(rawAtcCode.substring(0, 5)),
                    chemicalSubstance = maybeAtcLevel(if (rawAtcCode.length > ATC_LENGTH_4_LEVELS) rawAtcCode.substring(0, 7) else null)
                )
            } else {
                LOGGER.warn("ATC code $rawAtcCode did not contain at least 4 levels of classification. Ignoring ATC code for this medication")
                null
            }
        } else {
            null
        }
    }

    override fun resolveByName(name: String): Set<String> =
        atcMap.filterValues { it == name }.keys

    private fun maybeAtcLevel(levelCode: String?): AtcLevel? =
        levelCode?.let { atcLevel(it) }

    private fun atcLevel(levelCode: String) = AtcLevel(code = levelCode, name = lookup(levelCode))

    private fun lookup(level: String) =
        atcMap[level] ?: throw IllegalArgumentException("ATC code [$level] not found in tree")

    companion object {
        private val LOGGER = LogManager.getLogger(WhoAtcModel::class.java)
        
        fun createFromFile(tsvPath: String): WhoAtcModel {
            val lines = Files.readAllLines(File(tsvPath).toPath())
            val fields = TabularFile.createFields(lines[0].split(TabularFile.DELIMITER).dropLastWhile { it.isEmpty() }
                .toTypedArray())
            return WhoAtcModel(lines.map { it.split(TabularFile.DELIMITER).toTypedArray() }
                .associate { line -> line[fields["ATC code"]!!] to line[fields["ATC level name"]!!] })
        }
    }
}