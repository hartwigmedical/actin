package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

class HasHadSomeTreatmentsWithCategoryOfAllTypes(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>, private val minTreatmentLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory, category, { types.all { type -> it.isOfType(type) == true } }
        )
        val typesList = Format.concatItemsWithAnd(types)
        val baseMessage = "received at least $minTreatmentLines line(s) of $typesList combination ${category.display()}"

        return when {
            treatmentSummary.numSpecificMatches() >= minTreatmentLines -> {
                EvaluationFactory.pass("Has $baseMessage")
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches >= minTreatmentLines -> {
                EvaluationFactory.undetermined(
                    "Can't determine whether patient has $baseMessage",
                    "Undetermined if $baseMessage"
                )
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches
                    >= minTreatmentLines -> {
                EvaluationFactory.undetermined(
                    "Patient may have received at least $minTreatmentLines line(s) of ${category.display()} due to trial participation",
                    "Trial medication in history - undetermined if received at least $minTreatmentLines line(s) of ${category.display()}"
                )
            }

            else -> {
                EvaluationFactory.fail("Has not $baseMessage")
            }
        }
    }
}