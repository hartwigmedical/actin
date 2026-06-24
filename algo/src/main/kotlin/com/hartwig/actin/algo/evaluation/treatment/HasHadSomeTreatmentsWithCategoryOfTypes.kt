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
        val effectiveTreatmentHistory = record.oncologicalHistory + MedicationToTreatmentConverter.convert(record.medications ?: emptyList(), record.oncologicalHistory)

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            effectiveTreatmentHistory, category, { historyEntry -> historyEntry.matchesTypeFromSet(types) }
        )

        val typesList = Format.concatItemsWithOr(types)
        return when {
            treatmentSummary.numSpecificMatches() >= minTreatmentLines -> {
                EvaluationFactory.pass("Has received at least $minTreatmentLines line(s) of $typesList ${category.display()}")
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches >= minTreatmentLines -> {
                EvaluationFactory.undetermined("Undetermined if received at least $minTreatmentLines line(s) of $typesList ${category.display()}")
            }

            else -> {
                EvaluationFactory.fail("Has not received at least $minTreatmentLines line(s) of $typesList ${category.display()}")
            }
        }
    }
}