package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.medication.MedicationToTreatmentConverter

class HasHadSomeTreatmentsWithCategory(
    private val category: TreatmentCategory,
    private val minTreatmentLines: Int,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = MedicationToTreatmentConverter.convertAndCombine(record.medications, record.oncologicalHistory)

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(effectiveTreatmentHistory, category)

        return when {
            treatmentSummary.numSpecificMatches() >= minTreatmentLines -> {
                EvaluationFactory.pass(labels.hasHadSomeTreatmentsWithCategoryPass(minTreatmentLines, category.display()))
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numPossibleTrialMatches >= minTreatmentLines -> {
                EvaluationFactory.undetermined(
                    labels.hasHadSomeTreatmentsWithCategoryUndetermined(minTreatmentLines, category.display())
                )
            }

            else -> {
                EvaluationFactory.fail(labels.hasHadSomeTreatmentsWithCategoryFail(minTreatmentLines, category.display()))
            }
        }
    }
}