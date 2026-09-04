package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.medication.MedicationToTreatmentConverter

class HasHadSomeTreatmentsWithCategoryOfAllTypes(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>, private val minTreatmentLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = MedicationToTreatmentConverter.convertAndCombine(record.medications, record.oncologicalHistory)

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            effectiveTreatmentHistory, category, { historyEntry -> types.all { type -> historyEntry.isOfType(type) == true } }
        )

        val typesList = Format.concatItemsWithAnd(types)
        val baseMessage = "at least $minTreatmentLines line(s) of $typesList combination ${category.display()}"

        return when {
            treatmentSummary.numSpecificMatches() >= minTreatmentLines -> {
                EvaluationFactory.pass("At least $minTreatmentLines line(s) of $typesList combination ${category.display()} in provided treatments")
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches >= minTreatmentLines -> {
                EvaluationFactory.undetermined("Undetermined history of $baseMessage based on provided treatments")
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches >= minTreatmentLines -> {
                EvaluationFactory.undetermined("Trial medication - undetermined if at least $minTreatmentLines line(s) of ${category.display()} based on provided treatments")
            }

            else -> {
                EvaluationFactory.fail("Not at least $minTreatmentLines line(s) of $typesList combination ${category.display()} in provided treatments")
            }
        }
    }
}