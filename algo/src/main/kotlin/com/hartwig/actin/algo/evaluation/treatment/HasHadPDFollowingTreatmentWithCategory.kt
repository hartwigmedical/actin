package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadPDFollowingTreatmentWithCategory(
    private val category: TreatmentCategory,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val history = record.oncologicalHistory
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            history,
            category,
            { entry -> ProgressiveDiseaseFunctions.treatmentResultedInPD(entry, history) },
            { true },
            { entry -> ProgressiveDiseaseFunctions.treatmentResultedInPD(entry, history) != false }
        )

        return if (treatmentSummary.hasSpecificMatch()) {
            pass(labels.hasHadPDFollowingTreatmentWithCategoryPass(category.display()))
        } else if (treatmentSummary.hasApproximateMatch()) {
            undetermined(labels.hasHadPDFollowingTreatmentWithCategoryUndeterminedApproximate(category.display()))
        } else if (treatmentSummary.hasPossibleTrialMatch()) {
            undetermined(labels.hasHadPDFollowingTreatmentWithCategoryUndeterminedTrial(category.toString()))
        } else {
            fail(labels.hasHadPDFollowingTreatmentWithCategoryFail(category.display()))
        }
    }
}
