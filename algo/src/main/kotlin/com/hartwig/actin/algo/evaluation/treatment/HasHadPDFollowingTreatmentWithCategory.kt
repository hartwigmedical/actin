package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadPDFollowingTreatmentWithCategory(private val category: TreatmentCategory) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.clinical.oncologicalHistory,
            category,
            ProgressiveDiseaseFunctions::treatmentResultedInPD,
            { true },
            { entry -> ProgressiveDiseaseFunctions.treatmentResultedInPD(entry) != false }
        )

        return if (treatmentSummary.hasSpecificMatch()) {
            pass(
                "Patient has had progressive disease following treatment with category " + category.display(),
                "Has had " + category.display() + " treatment with PD"
            )
        } else if (treatmentSummary.hasApproximateMatch()) {
            undetermined(
                "Patient has had treatment with category " + category.display() + " but unclear PD status",
                "Has had " + category.display() + " treatment but undetermined PD status"
            )
        } else if (treatmentSummary.hasPossibleTrialMatch()) {
            undetermined(
                "Patient has had trial with unclear treatment category",
                "Trial treatment of unclear treatment category"
            )
        } else {
            fail(
                "Patient has no progressive disease following treatment with category " + category.display(),
                "Has not had " + category.display() + " treatment with PD"
            )
        }
    }
}