package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class IsEligibleForTreatmentOfCategoryAndType(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>,
    private val labels: EvaluationLabels.Treatment
): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory, category, { historyEntry -> historyEntry.matchesTypeFromSet(types) }
        )

        return when {
            treatmentSummary.hasSpecificMatch() -> {
                EvaluationFactory.warn(
                    labels.isEligibleForTreatmentOfCategoryAndTypeWarn(category.display(), Format.concatItemsWithOr(types))
                )
            }

            else -> {
                EvaluationFactory.recoverableUndetermined(
                    labels.isEligibleForTreatmentOfCategoryAndTypeRecoverableUndetermined(category.display(), Format.concatItemsWithOr(types))
                )
            }
        }
    }
}