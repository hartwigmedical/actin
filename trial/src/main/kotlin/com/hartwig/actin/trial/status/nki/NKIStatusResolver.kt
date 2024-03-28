package com.hartwig.actin.trial.status.nki

import com.hartwig.actin.trial.status.TrialStatus

private const val NKI_OPEN_STATUS = "OPEN"
private val NKI_CLOSED_STATUS = setOf("CLOSED", "PENDING", "COMPLETED", "WITHDRAWN")

object NKIStatusResolver {
    fun resolve(studyStatus: String): TrialStatus {
        return if (studyStatus == NKI_OPEN_STATUS) TrialStatus.OPEN else if (studyStatus in NKI_CLOSED_STATUS) TrialStatus.CLOSED else TrialStatus.UNINTERPRETABLE
    }
}