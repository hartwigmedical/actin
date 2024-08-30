package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.clinical.TumorDetails

object TumorDetailsInterpreter {
    const val CUP_LOCATION = "Unknown"
    const val CUP_SUB_LOCATION = "CUP"

    fun isCUP(tumor: TumorDetails): Boolean {
        val location = tumor.primaryTumorLocation
        val subLocation = tumor.primaryTumorSubLocation
        return location != null && subLocation != null && location == CUP_LOCATION && subLocation == CUP_SUB_LOCATION
    }
}