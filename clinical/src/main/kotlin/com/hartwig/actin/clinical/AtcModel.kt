package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.datamodel.AtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import com.hartwig.actin.util.TabularFile
import java.io.File
import java.nio.file.Files

class AtcModel(private val atcMap: Map<String, String>) {

    fun resolve(rawAtcCode: String): AtcClassification? {
        return if (rawAtcCode.trim().isNotEmpty() && rawAtcCode[0].lowercaseChar() in 'a'..'z') {
            return ImmutableAtcClassification.builder()
                .anatomicalMainGroup(atcLevel(rawAtcCode.substring(0, 1)))
                .therapeuticSubGroup(atcLevel(rawAtcCode.substring(0, 3)))
                .pharmacologicalSubGroup(atcLevel(rawAtcCode.substring(0, 4)))
                .chemicalSubGroup(atcLevel(rawAtcCode.substring(0, 5)))
                .chemicalSubstance(atcLevel(rawAtcCode.substring(0, 7))).build()
        } else {
            null
        }
    }

    private fun atcLevel(levelCode: String): ImmutableAtcLevel = ImmutableAtcLevel.builder().code(levelCode).name(lookup(levelCode)).build()

    private fun lookup(level: String) = atcMap[level] ?: throw IllegalArgumentException("ATC code [$level] not found in tree")

    companion object {
        fun createFromFile(tsvPath: String): AtcModel {
            val lines = Files.readAllLines(File(tsvPath).toPath())
            val fields = TabularFile.createFields(lines[0].split(TabularFile.DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray())
            return AtcModel(lines.map { it.split(TabularFile.DELIMITER).toTypedArray() }
                .associate { line -> line[fields["Name"]!!] to line[fields["ATCCode"]!!] })
        }
    }
}