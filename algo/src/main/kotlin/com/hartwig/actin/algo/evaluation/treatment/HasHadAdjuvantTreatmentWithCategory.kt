package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadAdjuvantTreatmentWithCategory(private val category: TreatmentCategory) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatments(record.clinical().priorTumorTreatments(), category) {
            it.name().lowercase().replace("neoadjuvant", "").contains("adjuvant")
        }

        return if (treatmentSummary.hasSpecificMatch()) {
            EvaluationFactory.pass("Has received adjuvant treatment(s) of ${category.display()}")
        } else {
            EvaluationFactory.fail("Has not received adjuvant treatment(s) of ${category.display()}")
        }
    }
}