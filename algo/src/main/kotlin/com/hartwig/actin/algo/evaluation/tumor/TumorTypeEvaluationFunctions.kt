package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.clinical.datamodel.TumorDetails

internal object TumorTypeEvaluationFunctions {
    fun hasTumorWithType(tumor: TumorDetails, validTypes: Set<String>): Boolean {
        return listOf(tumor.primaryTumorType, tumor.primaryTumorSubType).any { stringNotNullAndMatchesCollection(it, validTypes) }
    }

    fun hasTumorWithDetails(tumor: TumorDetails, validDetails: Set<String>): Boolean {
        return stringNotNullAndMatchesCollection(tumor.primaryTumorExtraDetails, validDetails)
    }

    fun hasPeritonealMetastases(tumor: TumorDetails): Boolean? {
        val targetTerms = listOf("peritoneum", "peritoneal", "intraperitoneum", "intraperitoneal")
        return tumor.otherLesions?.any { lesion ->
            val lowercaseLesion = lesion.lowercase()
            targetTerms.any(lowercaseLesion::startsWith) || targetTerms.any { lowercaseLesion.contains(" $it") }
        }
    }

    private fun stringNotNullAndMatchesCollection(nullableString: String?, collection: Collection<String>): Boolean {
        return nullableString != null && stringCaseInsensitivelyMatchesQueryCollection(nullableString, collection)
    }
}