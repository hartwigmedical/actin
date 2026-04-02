package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

object ChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCyclesEvaluator {

    fun treatmentmatches(
        treatmentHistoryEntries: List<TreatmentHistoryEntry>,
        type: TreatmentType,
        matches: (TreatmentHistoryEntry) -> Boolean?
    ): Set<Boolean?> =
        treatmentHistoryEntries.map {
            val matchingCategories = it.categories().containsAll(setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY))
            val chemotherapies = it.allTreatments().filter { treatment -> treatment.categories().contains(TreatmentCategory.CHEMOTHERAPY) }
            val matchingChemoType = chemotherapies.any { t -> type in t.types() }
            val allChemoTypesAreKnown = chemotherapies.all { t -> t.types().isNotEmpty() }

            when {
                matchingCategories && matchingChemoType && matches(it) == true -> true
                !matchingCategories && it.categories()
                    .isNotEmpty() || !matchingChemoType && allChemoTypesAreKnown || matches(it) == false -> false

                else -> null
            }
        }.toSet()
}