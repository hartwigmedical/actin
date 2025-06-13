package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.ingestion.FeedValidationWarning
import com.hartwig.feed.datamodel.FeedMeasurement

class VitalFunctionValidator {
    fun validate(patientId: String, feed: FeedMeasurement): FeedValidation {

        val emptyCodeDisplayValidation =
            FeedValidationWarning(patientId, "Empty vital function category").takeIf { feed.category.isEmpty() }

        val emptyQuantityValidation =
            FeedValidationWarning(patientId, "Empty vital function value").takeIf { feed.value.isNaN() }

        val noCategoryValidation = feed.category.takeIf { it.isNotEmpty() && VitalFunctionCategoryResolver.toCategory(it) == null }
            ?.let { FeedValidationWarning(patientId, "Invalid vital function category: $it") }

        val warnings = listOfNotNull(emptyCodeDisplayValidation, emptyQuantityValidation, noCategoryValidation)
        return FeedValidation(warnings.isEmpty(), warnings)
    }
}