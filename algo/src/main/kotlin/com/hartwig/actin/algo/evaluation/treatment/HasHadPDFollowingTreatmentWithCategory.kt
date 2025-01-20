package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadPDFollowingTreatmentWithCategory(private val category: TreatmentCategory) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory,
            category,
            ProgressiveDiseaseFunctions::treatmentResultedInPD,
            { true },
            { entry -> ProgressiveDiseaseFunctions.treatmentResultedInPD(entry) != false }
        )

        return if (treatmentSummary.hasSpecificMatch()) {
            pass("Has had " + category.display() + " treatment with PD")
        } else if (treatmentSummary.hasApproximateMatch()) {
            undetermined("Has had " + category.display() + " treatment but undetermined PD status")
        } else if (treatmentSummary.hasPossibleTrialMatch()) {
            undetermined("Undetermined if treatment received in previous trial included $category")
        } else {
            fail("Has not had " + category.display() + " treatment with PD")
        }
    }
}