package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadTreatmentWithCategoryButNotOfTypes(
    private val category: TreatmentCategory,
    private val ignoreTypes: List<String>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary =
            TreatmentSummaryForCategory.createForTreatments(
                record.clinical().priorTumorTreatments(),
                category
            ) { treatment ->
                ignoreTypes.none { TreatmentTypeResolver.isOfType(treatment, category, it) }
            }

        return when {
            treatmentSummary.hasSpecificMatch() -> EvaluationFactory.pass(
                "Has received ${category.display()} ignoring ${
                    concat(
                        ignoreTypes
                    )
                }"
            )

            treatmentSummary.hasPossibleTrialMatch() -> EvaluationFactory.undetermined(
                "Patient may have received ${category.display()} ignoring ${concat(ignoreTypes)} due to trial participation",
                "Undetermined if received ${category.display()} ignoring ${concat(ignoreTypes)} due to trial participation"
            )

            else -> EvaluationFactory.fail("Has not received ${category.display()} ignoring ${concat(ignoreTypes)}")
        }
    }
}