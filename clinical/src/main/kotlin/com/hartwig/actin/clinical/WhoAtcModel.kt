package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.datamodel.AtcClassification
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.util.TabularFile
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files

interface AtcModel {
    fun resolveByCode(rawAtcCode: String, rawAtcLevelName: String): AtcClassification?
    fun resolveByName(name: String): Set<String>
}

private const val ATC_LENGTH_4_LEVELS = 5

class WhoAtcModel(private val atcMap: Map<String, String>, private val atcOverrides: Map<Pair<String, String>, String>) : AtcModel {

    override fun resolveByCode(rawAtcCode: String, rawAtcLevelName: String): AtcClassification? {
        return if (rawAtcCode.trim().isNotEmpty() && rawAtcCode[0].lowercaseChar() in 'a'..'z') {
            return if (rawAtcCode.length >= ATC_LENGTH_4_LEVELS) {
                val correctedRawAtcCode = atcOverrides[Pair(rawAtcCode, rawAtcLevelName)] ?: rawAtcCode

                AtcClassification(
                    anatomicalMainGroup = atcLevel(correctedRawAtcCode.substring(0, 1)),
                    therapeuticSubGroup = atcLevel(correctedRawAtcCode.substring(0, 3)),
                    pharmacologicalSubGroup = atcLevel(correctedRawAtcCode.substring(0, 4)),
                    chemicalSubGroup = atcLevel(correctedRawAtcCode.substring(0, 5)),
                    chemicalSubstance = maybeAtcLevel(if (correctedRawAtcCode.length > ATC_LENGTH_4_LEVELS) correctedRawAtcCode.substring(0, 7) else null)
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

        fun createFromFiles(atcTreeTsvPath: String, atcOverridesTsvPath: String): WhoAtcModel {
            val (linesAtcTree, fieldsAtcTree) = readTsv(atcTreeTsvPath)
            val (linesAtcOverrides, fieldsAtcOverrides) = readTsv(atcOverridesTsvPath)
            return WhoAtcModel(
                linesAtcTree.map { it.split(TabularFile.DELIMITER).toTypedArray() }
                    .associate { line -> line[fieldsAtcTree["ATC code"]!!] to line[fieldsAtcTree["ATC level name"]!!] },
                linesAtcOverrides.map { it.split(TabularFile.DELIMITER).toTypedArray() }.associate { line ->
                    Pair(
                        line[fieldsAtcOverrides["Previous ATC code"]!!],
                        line[fieldsAtcOverrides["ATC level name"]!!]
                    ) to line[fieldsAtcOverrides["New ATC code"]!!]
                })
        }

        private fun readTsv(tsv: String): Pair<List<String>, Map<String, Int>> {
            val lines = Files.readAllLines(File(tsv).toPath())
            val fields = TabularFile.createFields(lines[0].split(TabularFile.DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray())
            return Pair(lines, fields)
        }
    }
}