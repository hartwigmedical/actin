package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.medication.MedicationToTreatmentConverter

class HasHadSomeTreatmentsWithCategoryOfAllTypes(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>,
    private val minTreatmentLines: Int,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = MedicationToTreatmentConverter.convertAndCombine(record.medications, record.oncologicalHistory)

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            effectiveTreatmentHistory, category, { historyEntry -> types.all { type -> historyEntry.isOfType(type) == true } }
        )

        val typesList = Format.concatItemsWithAnd(types)

        return when {
            treatmentSummary.numSpecificMatches() >= minTreatmentLines -> {
                EvaluationFactory.pass(labels.hasHadSomeTreatmentsWithCategoryOfAllTypesPass(minTreatmentLines, typesList, category.display()))
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches >= minTreatmentLines -> {
                EvaluationFactory.undetermined(
                    labels.hasHadSomeTreatmentsWithCategoryOfAllTypesUndetermined(minTreatmentLines, typesList, category.display())
                )
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches >= minTreatmentLines -> {
                EvaluationFactory.undetermined(
                    labels.hasHadSomeTreatmentsWithCategoryOfAllTypesUndeterminedTrial(minTreatmentLines, category.display())
                )
            }

            else -> {
                EvaluationFactory.fail(labels.hasHadSomeTreatmentsWithCategoryOfAllTypesFail(minTreatmentLines, typesList, category.display()))
            }
        }
    }
}