package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.medication.MedicationToTreatmentConverter

class HasHadSomeTreatmentsWithCategoryOfTypes(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>, private val minTreatmentLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = MedicationToTreatmentConverter.convertAndCombine(record.medications, record.oncologicalHistory)

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            effectiveTreatmentHistory, category, { historyEntry -> historyEntry.matchesTypeFromSet(types) }
        )

        val typesList = Format.concatItemsWithOr(types)
        return when {
            treatmentSummary.numSpecificMatches() >= minTreatmentLines -> {
                EvaluationFactory.pass("At least $minTreatmentLines line(s) of $typesList ${category.display()} in provided treatments")
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches >= minTreatmentLines -> {
                EvaluationFactory.undetermined("Undetermined history of at least $minTreatmentLines line(s) of $typesList ${category.display()} based on provided treatments")
            }

            else -> {
                EvaluationFactory.fail("Not at least $minTreatmentLines line(s) of $typesList ${category.display()} in provided treatments")
            }
        }
    }
}