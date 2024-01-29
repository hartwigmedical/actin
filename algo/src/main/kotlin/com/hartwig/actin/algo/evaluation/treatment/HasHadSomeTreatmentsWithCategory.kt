package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadSomeTreatmentsWithCategory(private val category: TreatmentCategory, private val minTreatmentLines: Int) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(record.clinical.oncologicalHistory, category)

        return if (treatmentSummary.numSpecificMatches() >= minTreatmentLines) {
            EvaluationFactory.pass(
                "Patient has received at least $minTreatmentLines line(s) of ${category.display()}",
                "Has received at least $minTreatmentLines line(s) of ${category.display()}"
            )
        } else if (treatmentSummary.numSpecificMatches() + treatmentSummary.numPossibleTrialMatches >= minTreatmentLines) {
            EvaluationFactory.undetermined(
                "Patient may have received at least $minTreatmentLines line(s) of  ${category.display()} due to trial participation",
                "Undetermined if received at least $minTreatmentLines line(s) of ${category.display()} due to trial participation"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received at least $minTreatmentLines line(s) of ${category.display()}",
                "Has not received at least $minTreatmentLines line(s) of ${category.display()}"
            )
        }
    }
}