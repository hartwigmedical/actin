package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

class HasHadTreatmentWithCategoryButNotOfTypes(
    private val category: TreatmentCategory,
    private val ignoreTypes: Set<TreatmentType>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary =
            TreatmentSummaryForCategory.createForTreatmentHistory(record.oncologicalHistory, category) { treatment ->
                ignoreTypes.none { treatment.isOfType(it) ?: false }
            }

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