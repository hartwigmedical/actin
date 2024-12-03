package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.datamodel.clinical.Cyp

object MedicationUtil {

    fun extractCypString(cyp: Cyp): String {
        return cyp.toString().substring(3).replace("_", "/")
    }
}