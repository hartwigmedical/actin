package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.clinical.datamodel.Complication

object ComplicationFunctions {
    fun findComplicationNamesMatchingAnyCategory(record: PatientRecord, categorySearchTerms: List<String>): Set<String> {
        return record.clinical().complications()
            ?.filter { it.categories().any { category -> stringCaseInsensitivelyMatchesQueryCollection(category, categorySearchTerms) } }
            ?.map { it.name() }?.toSet() ?: emptySet()
    }

    fun findComplicationCategoriesMatchingAnyCategory(record: PatientRecord, categorySearchTerms: List<String>): Set<String> {
        return record.clinical().complications()?.flatMap { it.categories() }
            ?.filter { stringCaseInsensitivelyMatchesQueryCollection(it, categorySearchTerms) }?.toSet() ?: emptySet()
    }

    fun isYesInputComplication(complication: Complication): Boolean {
        return complication.name().isEmpty() && complication.categories().isEmpty()
    }
}