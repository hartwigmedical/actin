package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.util.TabularFile
import java.io.File
import java.nio.file.Files

object CurationConfigFile {

    fun <T : CurationConfig> read(tsv: String, factory: CurationConfigFactory<T>): List<ValidatedCurationConfig<T>> {
        val (lines, fields) = readTsv(tsv)
        return lines.subList(1, lines.size)
            .map { factory.create(fields, it.split(TabularFile.DELIMITER).toTypedArray()) }
    }

    fun readTsv(tsv: String): Pair<List<String>, Map<String, Int>> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = TabularFile.createFields(lines[0].split(TabularFile.DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray())
        return Pair(lines, fields)
    }
}