package com.hartwig.actin.treatment

import com.hartwig.actin.util.TabularFile
import java.io.File
import java.nio.file.Files

object FileUtil {

    private const val TSV_DELIMITER = "\t"

    fun <T> createObjectsFromTsv(tsv: String, create: (Map<String, Int>, List<String>) -> T): List<T> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = TabularFile.createFields(lines[0].split(TSV_DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray())
        return lines.subList(1, lines.size).map { create(fields, it.split(TSV_DELIMITER)) }
    }
}