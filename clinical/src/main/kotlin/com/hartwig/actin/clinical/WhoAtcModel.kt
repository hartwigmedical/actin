package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.datamodel.AtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import com.hartwig.actin.util.TabularFile
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files

interface AtcModel {
    fun resolve(rawAtcCode: String): AtcClassification?
}

class WhoAtcModel(private val atcMap: Map<String, String>) : AtcModel {

    override fun resolve(rawAtcCode: String): AtcClassification? {
        return if (rawAtcCode.trim().isNotEmpty() && rawAtcCode[0].lowercaseChar() in 'a'..'z') {
            if (rawAtcCode.length == 8) {
                ImmutableAtcClassification.builder()
                    .anatomicalMainGroup(atcLevel(rawAtcCode.substring(0, 1)))
                    .therapeuticSubGroup(atcLevel(rawAtcCode.substring(0, 3)))
                    .pharmacologicalSubGroup(atcLevel(rawAtcCode.substring(0, 4)))
                    .chemicalSubGroup(atcLevel(rawAtcCode.substring(0, 5)))
                    .chemicalSubstance(atcLevel(rawAtcCode.substring(0, 7))).build()
            }else{
                LOGGER.warn("ATC code with incorrect length [{}]", rawAtcCode)
                null
            }
        } else {
            null
        }
    }

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