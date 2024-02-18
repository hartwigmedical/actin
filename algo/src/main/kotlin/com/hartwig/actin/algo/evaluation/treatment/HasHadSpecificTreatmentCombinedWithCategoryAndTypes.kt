package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

class HasHadSpecificTreatmentCombinedWithCategoryAndTypes(
    private val treatment: Treatment,
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        if (types.isNullOrEmpty()) return EvaluationFactory.fail("Types not provided")

        if (!record.clinical.oncologicalHistory.any { history ->
                history.allTreatments().any { pastTreatment -> pastTreatment.name.lowercase() == treatment.name.lowercase() }
            }) return EvaluationFactory.fail("Has not received ${treatment}")

        val treatmentSummary =
            TreatmentSummaryForCategory.createForTreatmentHistory(record.clinical.oncologicalHistory, category) {
                it.matchesTypeFromSet(types)
            }

        val treatmentLine = "${concatItems(types)} ${category.display()}"
        return when {
            treatmentSummary.numSpecificMatches() > 0 -> {
                EvaluationFactory.pass("Has received ${treatmentLine}")
            }

            treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches > 0 -> {
                EvaluationFactory.undetermined(
                    "Patient may have received ${treatmentLine} during past trial participation",
                    "Can't determine whether patient has received ${treatmentLine}"
                )
            }

            else -> {
                EvaluationFactory.fail("Has not received ${treatmentLine}")
            }
        }
    }
}