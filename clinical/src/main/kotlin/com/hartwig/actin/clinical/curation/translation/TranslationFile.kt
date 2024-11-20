package com.hartwig.actin.clinical.curation.translation

import com.hartwig.actin.util.TabularFile
import java.io.File
import java.nio.file.Files

object TranslationFile {

    private const val DELIMITER = "\t"

    fun <T> read(tsv: String, factory: TranslationFactory<T>): List<T> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = TabularFile.createFields(lines[0].split(DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        return lines.subList(1, lines.size).map { line ->
            factory.create(fields, line.split(DELIMITER.toRegex()).toTypedArray())
        }
    }
}