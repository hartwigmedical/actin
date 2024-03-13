package com.hartwig.actin.trial.nki

class NKITrialDatabase(
    val entries: List<NKITrialStatus>
) {

    fun findStatus(studyId: String): NKITrialStatus? {
        return entries.find { it.studyId == studyId }
    }
}