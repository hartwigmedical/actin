package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadSpecificTreatmentSinceDateTest : TreatmentSinceDateFunctionsTestAbstract() {
    private val treatmentQuery = DrugTreatment(
        name = "treatment", drugs = setOf(Drug(name = "Chemo drug", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = emptySet()))
    )

    override fun functionForDate(minDate: LocalDate): EvaluationFunction {
        return HasHadSpecificTreatmentSinceDate(treatmentQuery, minDate)
    }

    override fun matchingTreatment(stopYear: Int?, stopMonth: Int?, startYear: Int?, startMonth: Int?): TreatmentHistoryEntry {
        return treatmentHistoryEntry(setOf(treatmentQuery), startYear, startMonth, stopYear = stopYear, stopMonth = stopMonth)
    }
}