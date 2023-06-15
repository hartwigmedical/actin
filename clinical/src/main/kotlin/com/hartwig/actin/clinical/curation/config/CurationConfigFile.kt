package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.util.TabularFile
import java.io.File
import java.io.IOException
import java.nio.file.Files

object CurationConfigFile {
    private const val DELIMITER = "\t"

    @Throws(IOException::class)
    fun <T : CurationConfig> read(tsv: String, factory: CurationConfigFactory<T>): List<T> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = TabularFile.createFields(lines[0].split(DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        return lines.subList(1, lines.size).map { factory.create(fields, it.split(DELIMITER.toRegex()).toTypedArray()) }
    }
}