package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadTreatmentWithDrugSinceDateTest : TreatmentSinceDateFunctionsTestAbstract() {
    private val matchingDrug = Drug(name = "Chemo drug", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = emptySet())

    override fun functionForDate(minDate: LocalDate): EvaluationFunction {
        return HasHadTreatmentWithAnyDrugSinceDate(
            setOf(matchingDrug, Drug(name = "other", category = TreatmentCategory.IMMUNOTHERAPY, drugTypes = emptySet())),
            minDate
        )
    }

    override fun matchingTreatment(stopYear: Int?, stopMonth: Int?, startYear: Int?, startMonth: Int?): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            setOf(DrugTreatment(name = "treatment", drugs = setOf(matchingDrug))),
            startYear,
            startMonth,
            stopYear = stopYear,
            stopMonth = stopMonth
        )
    }
}