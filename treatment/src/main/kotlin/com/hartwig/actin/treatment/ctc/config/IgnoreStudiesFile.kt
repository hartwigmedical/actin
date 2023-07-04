package com.hartwig.actin.treatment.ctc.config

import com.hartwig.actin.treatment.FileUtil
import java.io.IOException

object IgnoreStudiesFile {

    @Throws(IOException::class)
    fun read(tsv: String): Set<String> {
        return FileUtil.createObjectsFromTsv(tsv, ::create).toSet()
    }

    private fun create(fields: Map<String, Int>, parts: List<String>): String = parts[fields["studyMETCToIgnore"]!!]
}