package com.hartwig.actin.clinical.curation.translation

import com.google.common.collect.Lists
import com.hartwig.actin.util.TabularFile
import java.io.File
import java.io.IOException
import java.nio.file.Files

object TranslationFile {
    private const val DELIMITER = "\t"

    @Throws(IOException::class)
    fun <T : Translation?> read(tsv: String, factory: TranslationFactory<T>): List<T> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val translations: MutableList<T> = Lists.newArrayList()
        val fields = TabularFile.createFields(lines[0].split(DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        for (line in lines.subList(1, lines.size)) {
            translations.add(factory.create(fields, line.split(DELIMITER.toRegex()).toTypedArray()))
        }
        return translations
    }
}