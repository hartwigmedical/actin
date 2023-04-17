package com.hartwig.actin.soc.evaluation.common

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.soc.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection

object ComplicationFunctions {
    fun findComplicationNamesMatchingAnyCategory(record: PatientRecord, categorySearchTerms: List<String>): Set<String> {
        return findComplicationsMatchingAnyCategory(record, categorySearchTerms)?.map { it.name() }?.toSet() ?: emptySet()
    }

    fun findComplicationCategoriesMatchingAnyCategory(record: PatientRecord, categorySearchTerms: List<String>): Set<String> {
        return findComplicationsMatchingAnyCategory(record, categorySearchTerms)
                ?.flatMap { it.categories() }
                ?.toSet()
                ?: emptySet()
    }

    fun isYesInputComplication(complication: Complication): Boolean {
        return complication.name().isEmpty() && complication.categories().isEmpty()
    }

    private fun findComplicationsMatchingAnyCategory(record: PatientRecord, categorySearchTerms: List<String>): List<ImmutableComplication>? {
        return record.clinical().complications()?.map { complication: Complication -> ImmutableComplication.builder()
                .from(complication)
                .categories(complication.categories().filter { stringCaseInsensitivelyMatchesQueryCollection(it, categorySearchTerms) })
                .build()
        }
        ?.filter { complication: ImmutableComplication -> complication.categories().toSet().isNotEmpty() }
    }
}