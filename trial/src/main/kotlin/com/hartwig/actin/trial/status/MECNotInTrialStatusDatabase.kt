package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.FileUtil

object MECNotInTrialStatusDatabase {

    fun read(tsv: String): Set<String> {
        return FileUtil.createObjectsFromTsv(tsv, ::create).toSet()
    }

    private fun create(fields: Map<String, Int>, parts: List<String>): String = parts[fields["MECStudyNotInTrialStatusDatabase"]!!]
}