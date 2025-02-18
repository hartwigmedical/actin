package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import java.time.LocalDate

class HasHadSomeTreatmentsWithCategoryWithIntentsRecently(
    private val category: TreatmentCategory,
    private val intentsToFind: Set<Intent>,
    private val minDate: LocalDate
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return HasHadSomeTreatmentsWithCategoryWithIntents(category, intentsToFind, minDate).evaluate(record)
    }
}