package com.hartwig.actin.treatment.trial.config

import com.hartwig.actin.util.TabularFile
import java.io.File
import java.io.IOException
import java.nio.file.Files

object TrialConfigFile {
    private const val DELIMITER = "\t"

    @Throws(IOException::class)
    fun <T : TrialConfig> read(tsv: String, factory: TrialConfigFactory<T>): List<T> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = TabularFile.createFields(lines[0].split(DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray())

        return lines.subList(1, lines.size).map { factory.create(fields, it.split(DELIMITER).toTypedArray()) }
    }
}