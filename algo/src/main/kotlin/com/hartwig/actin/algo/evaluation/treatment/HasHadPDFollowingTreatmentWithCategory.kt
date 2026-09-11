package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadPDFollowingTreatmentWithCategory(private val category: TreatmentCategory) : EvaluationFunction {

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
            pass(category.display() + " treatment in provided treatments with PD")
        } else if (treatmentSummary.hasApproximateMatch()) {
            undetermined(category.display() + " treatment in provided treatments but PD status is undetermined")
        } else if (treatmentSummary.hasPossibleTrialMatch()) {
            undetermined("Undetermined if treatment from previous trial included $category")
        } else {
            fail("No " + category.display() + " treatment with PD in provided treatments")
        }
    }
}