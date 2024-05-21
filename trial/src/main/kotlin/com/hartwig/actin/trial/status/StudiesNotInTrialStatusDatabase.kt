package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.FileUtil

object StudiesNotInTrialStatusDatabase {

    fun read(tsv: String): Set<String> {
        return FileUtil.createObjectsFromTsv(tsv, ::create).toSet()
    }

    private fun create(fields: Map<String, Int>, parts: List<String>): String {
        // TODO (KD): Remove support for "StudyNotInTrialStatusDatabase" once resorces have been updated.
        return if (fields.containsKey("studyNotInTrialStatusDatabase")) {
            parts[fields["studyNotInTrialStatusDatabase"]!!]
        } else {
            parts[fields["StudyNotInTrialStatusDatabase"]!!]
        }
    }
}