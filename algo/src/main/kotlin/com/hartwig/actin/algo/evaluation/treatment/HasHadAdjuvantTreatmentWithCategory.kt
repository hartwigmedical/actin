package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

class HasHadAdjuvantTreatmentWithCategory(private val category: TreatmentCategory) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory,
            category,
            { it.intents?.contains(Intent.ADJUVANT) == true },
            { true },
            { it.intents?.contains(Intent.ADJUVANT) != false }
        )

        return if (treatmentSummary.hasSpecificMatch()) {
            EvaluationFactory.pass("Has received adjuvant treatment(s) of ${category.display()}")
        } else {
            EvaluationFactory.fail("Has not received adjuvant treatment(s) of ${category.display()}")
        }
    }
}