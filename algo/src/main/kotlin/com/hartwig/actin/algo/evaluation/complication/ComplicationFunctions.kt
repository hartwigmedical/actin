package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.datamodel.clinical.Complication

object ComplicationFunctions {

    fun isYesInputComplication(complication: Complication): Boolean {
        return complication.name.isEmpty()
    }
}