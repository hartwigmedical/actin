package com.hartwig.actin.treatment.ctc.config

import com.hartwig.actin.treatment.FileUtil
import com.hartwig.actin.util.ResourceFile
import java.io.IOException

object UnmappedCohortFile {
    @Throws(IOException::class)
    fun read(tsv: String): Set<Int> {
        return FileUtil.createObjectsFromTsv(tsv, ::create).toSet()
    }

    private fun create(fields: Map<String, Int>, parts: List<String>): Int = ResourceFile.integer(parts[fields["cohortId"]!!])
}