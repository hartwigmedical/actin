package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadSomeTreatmentsWithCategoryOfTypes(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>, private val minTreatmentLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory, category, { historyEntry -> historyEntry.matchesTypeFromSet(types) }
        )

        val priorCancerMedication = record.medications
            ?.filter { medication ->
                (medication.drug?.category?.equals(category) == true && medication.drug?.drugTypes?.any {
                    types.contains(
                        it
                    )
                } == true)
            } ?: emptyList()

        val typesList = concatItems(types)
        return when {
            treatmentSummary.numSpecificMatches() >= minTreatmentLines || (minTreatmentLines == 1 && priorCancerMedication.isNotEmpty()) -> {
                EvaluationFactory.pass("Has received at least $minTreatmentLines line(s) of $typesList ${category.display()}")
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches >= minTreatmentLines || (minTreatmentLines == 1 && record.medications?.any { it.isTrialMedication } == true) -> {
                EvaluationFactory.undetermined(
                    "Can't determine whether patient has received at least $minTreatmentLines line(s) of $typesList ${category.display()}",
                    "Undetermined if received at least $minTreatmentLines line(s) of $typesList ${category.display()}"
                )
            }

            else -> {
                EvaluationFactory.fail("Has not received at least $minTreatmentLines line(s) of $typesList ${category.display()}")
            }
        }
    }
}