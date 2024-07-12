package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.FileUtil
import com.hartwig.actin.util.ResourceFile

object UnmappedCohortFile {

    fun read(tsv: String): Set<String> {
        return FileUtil.createObjectsFromTsv(tsv, UnmappedCohortFile::create).toSet()
    }

    private fun create(fields: Map<String, Int>, parts: List<String>): String = ResourceFile.optionalString(parts[fields["cohortId"]!!])!!
}