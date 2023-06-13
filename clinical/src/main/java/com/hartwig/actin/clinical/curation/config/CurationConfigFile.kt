package com.hartwig.actin.clinical.curation.config

import com.google.common.collect.Lists
import com.hartwig.actin.util.TabularFile
import java.io.File
import java.io.IOException
import java.nio.file.Files

object CurationConfigFile {
    private const val DELIMITER = "\t"

    @Throws(IOException::class)
    fun <T : CurationConfig?> read(tsv: String, factory: CurationConfigFactory<T>): List<T> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val configs: MutableList<T> = Lists.newArrayList()
        val fields = TabularFile.createFields(lines[0].split(DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        for (line in lines.subList(1, lines.size)) {
            configs.add(factory.create(fields, line.split(DELIMITER.toRegex()).toTypedArray()))
        }
        return configs
    }
}