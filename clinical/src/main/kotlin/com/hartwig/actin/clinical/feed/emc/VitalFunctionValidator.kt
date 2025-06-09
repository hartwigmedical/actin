package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.ingestion.FeedValidationWarning
import com.hartwig.feed.datamodel.FeedMeasurement

class VitalFunctionValidator {
    fun validate(patientId: String, feed: FeedMeasurement): FeedValidation {

        val emptyCodeDisplayValidation = feed.category.takeIf { it.isEmpty() }
            ?.let { listOf(FeedValidationWarning(patientId, "Empty vital function category")) }

        val emptyQuantityValidation = feed.value.takeIf { it.isNaN() }
            ?.let { listOf(FeedValidationWarning(patientId, "Empty vital function value")) }

        val noCategoryValidation = feed.category.takeIf { it.isNotEmpty() && VitalFunctionCategoryResolver.toCategory(it) == null }
            ?.let {
                listOf(FeedValidationWarning(patientId, "Invalid vital function category: $it"))
            }

        val warnings = listOfNotNull(emptyCodeDisplayValidation, emptyQuantityValidation, noCategoryValidation).flatten()
        return FeedValidation(warnings.isEmpty(), warnings)
    }
}