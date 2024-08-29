package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadTreatmentWithCategoryButNotOfTypes(
    private val category: TreatmentCategory,
    private val ignoreTypes: Set<TreatmentType>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory, category, { historyEntry -> ignoreTypes.none { historyEntry.isOfType(it) == true } }
        )

        val ignoreTypesList = concatItems(ignoreTypes)
        return when {
            treatmentSummary.hasSpecificMatch() -> EvaluationFactory.pass("Has received ${category.display()} ignoring $ignoreTypesList")

            treatmentSummary.hasPossibleTrialMatch() -> EvaluationFactory.undetermined(
                "Patient may have received ${category.display()} ignoring $ignoreTypesList due to trial participation",
                "Undetermined if received ${category.display()} ignoring $ignoreTypesList due to trial participation"
            )

            else -> EvaluationFactory.fail("Has not received ${category.display()} ignoring $ignoreTypesList")
        }
    }
}