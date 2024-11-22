package com.hartwig.actin.medication

import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.util.TabularFile
import java.io.File
import java.nio.file.Files

class AtcTree(private val atcMap: Map<String, String>) {

    fun resolve(rawAtcCode: String): AtcLevel {
        val atcName = atcMap[rawAtcCode]
        return atcName?.let { AtcLevel(name = atcName, code = rawAtcCode) }
            ?: throw IllegalArgumentException("ATC code [$rawAtcCode] not found in tree")
    }

    companion object {
        fun createFromFile(tsvPath: String): AtcTree {
            val lines = Files.readAllLines(File(tsvPath).toPath())
            val fields = TabularFile.createFields(lines[0].split(TabularFile.DELIMITER).dropLastWhile { it.isEmpty() }
                .toTypedArray())
            return AtcTree(lines.map { it.split(TabularFile.DELIMITER).toTypedArray() }
                .associate { line -> line[fields["ATC code"]!!] to line[fields["ATC level name"]!!] })
        }
    }
}