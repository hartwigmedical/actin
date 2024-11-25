package com.hartwig.actin.icd.serialization

import com.hartwig.actin.icd.datamodel.ClassKind
import com.hartwig.actin.icd.datamodel.IcdNode
import com.hartwig.actin.util.TabularFile
import java.io.File
import java.nio.file.Files

object IcdDeserializer {

    fun readFromFile(tsvPath: String): List<IcdNode> {
        val lines = Files.readAllLines(File(tsvPath).toPath())
        val fields = TabularFile.createFields(lines[0].split(TabularFile.DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray())

        val icdNodeList = lines.drop(1).map { line ->
            val values = line.split(TabularFile.DELIMITER).toTypedArray()

            IcdNode(
                foundationUri = readNullableField(fields, values, "Foundation URI"),
                linearizationUri = values[fields["Linearization URI"]!!],
                code = solveCode(fields, values),
                blockId = readNullableField(fields, values, "BlockId"),
                title = values[fields["Title"]!!],
                classKind = resolveClassKind(fields, values),
                depthInKind = values[fields["DepthInKind"]!!].toInt(),
                isResidual = values[fields["IsResidual"]!!].toBoolean(),
                chapterNo = values[fields["ChapterNo"]!!],
                browserLink = values[fields["BrowserLink"]!!],
                isLeaf = values[fields["isLeaf"]!!].toBoolean(),
                primaryTabulation = readNullableField(fields, values, "Primary tabulation")?.toBoolean(),
                grouping1 = readNullableField(fields, values, "Grouping1"),
                grouping2 = readNullableField(fields, values, "Grouping2"),
                grouping3 = readNullableField(fields, values, "Grouping3"),
                grouping4 = readNullableField(fields, values, "Grouping4"),
                grouping5 = readNullableField(fields, values, "Grouping5")
            )
        }

        return icdNodeList.filterNot { it.chapterNo.any { char -> char.isLetter() } }
    }

    private fun readNullableField(fields: Fields, values: Array<String>, column: String): String? =
        fields[column]?.let { field -> values.getOrNull(field)?.takeIf { it.isNotBlank() } }

    private fun resolveClassKind(fields: Fields, values: Array<String>): ClassKind {
        return ClassKind.valueOf(values[fields["ClassKind"]!!].uppercase())
    }

    private fun solveCode(fields: Fields, values: Array<String>): String {
        return when (resolveClassKind(fields, values)) {
            ClassKind.CHAPTER -> values[fields["ChapterNo"]!!]
            ClassKind.BLOCK -> values[fields["BlockId"]!!]
            else -> values[fields["Code"]!!]
        }
    }
}

private typealias Fields = Map<String, Int>