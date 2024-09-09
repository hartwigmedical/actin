package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadSomeTreatmentsWithCategory(private val category: TreatmentCategory, private val minTreatmentLines: Int) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(record.oncologicalHistory, category)

        val hadCancerMedicationWithCategory =
            record.medications?.any { medication -> MedicationFunctions.hasCategory(medication, category) } ?: false

        return when {
            treatmentSummary.numSpecificMatches() >= minTreatmentLines || (minTreatmentLines == 1 && hadCancerMedicationWithCategory) -> {
                EvaluationFactory.pass(
                    "Patient has received at least $minTreatmentLines line(s) of ${category.display()}",
                    "Has received at least $minTreatmentLines line(s) of ${category.display()}"
                )
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numPossibleTrialMatches >= minTreatmentLines || (minTreatmentLines == 1 && record.medications?.any { it.isTrialMedication } == true) -> {
                EvaluationFactory.undetermined(
                    "Patient may have received at least $minTreatmentLines line(s) of  ${category.display()} due to trial participation",
                    "Undetermined if received at least $minTreatmentLines line(s) of ${category.display()} due to trial participation"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not received at least $minTreatmentLines line(s) of ${category.display()}",
                    "Has not received at least $minTreatmentLines line(s) of ${category.display()}"
                )
            }
        }
    }
}