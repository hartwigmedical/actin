package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.evaluation.complication.ComplicationFunctions

object WHOFunctions {

    val COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS: List<String> =
        mutableListOf("Ascites", "Pleural effusion", "Pericardial effusion", "Pain", "Spinal cord compression")

    fun findComplicationCategoriesAffectingWHOStatus(record: PatientRecord): Set<String> {
        return ComplicationFunctions.findComplicationCategoriesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS)
    }
}