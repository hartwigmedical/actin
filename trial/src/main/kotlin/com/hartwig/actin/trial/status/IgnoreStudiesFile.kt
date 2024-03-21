package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.FileUtil

object IgnoreStudiesFile {

    fun read(tsv: String): Set<String> {
        return FileUtil.createObjectsFromTsv(tsv, IgnoreStudiesFile::create).toSet()
    }

    private fun create(fields: Map<String, Int>, parts: List<String>): String = parts[fields["studyMETCToIgnore"]!!]
}