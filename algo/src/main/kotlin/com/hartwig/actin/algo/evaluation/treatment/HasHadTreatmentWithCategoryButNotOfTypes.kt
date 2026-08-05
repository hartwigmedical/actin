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

class HasHadTreatmentWithCategoryButNotOfTypes(
    private val category: TreatmentCategory,
    private val ignoreTypes: Set<TreatmentType>,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = MedicationToTreatmentConverter.convertAndCombine(record.medications, record.oncologicalHistory)

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            effectiveTreatmentHistory,
            category,
            { historyEntry -> ignoreTypes.none { historyEntry.isOfType(it) == true } }
        )

        val ignoreTypesList = Format.concatItemsWithAnd(ignoreTypes)
        return when {
            treatmentSummary.hasSpecificMatch() -> EvaluationFactory.pass(
                labels.hasHadTreatmentWithCategoryButNotOfTypesPass(category.display(), ignoreTypesList)
            )

            treatmentSummary.hasPossibleTrialMatch() -> EvaluationFactory.undetermined(
                labels.hasHadTreatmentWithCategoryButNotOfTypesUndetermined(category.display(), ignoreTypesList)
            )

            else -> EvaluationFactory.fail(labels.hasHadTreatmentWithCategoryButNotOfTypesFail(category.display(), ignoreTypesList))
        }
    }
}