package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

class HasHadSpecificTreatmentCombinedWithCategoryAndOptionallyTypes(
    private val treatment: Treatment,
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentDesc =
            "combined therapy with ${treatment.name} and ${types?.let { " ${concatItemsWithAnd(types)}" } ?: ""} ${category.display()}"

        val relevantHistory = record.clinical.oncologicalHistory.filter { history ->
            history.allTreatments().any { pastTreatment -> pastTreatment.name.lowercase() == treatment.name.lowercase() }
        }
        if (relevantHistory.isEmpty()) return EvaluationFactory.fail("Patient has not received ${treatment.name}")

        if (relevantHistory.any { history ->
                history.allTreatments().any { pastTreatment ->
                    treatmentContainsMatch(pastTreatment)
                }
            }) {
            return EvaluationFactory.pass("Patient has received $treatmentDesc")
        }

        if (record.clinical.oncologicalHistory.any { history ->
                history.allTreatments().any { pastTreatment ->
                    treatmentContainsMatch(pastTreatment)
                }
            }) {
            return EvaluationFactory.undetermined(
                "Patient may have received $treatmentDesc during past trial participation",
                "Can't determine whether patient has received $treatmentDesc"
            )
        }
        return EvaluationFactory.fail("Patient has not received $treatmentDesc")
    }

    private fun treatmentContainsMatch(candidate: Treatment): Boolean {
        val lookForTypes = if (types.isNullOrEmpty()) emptySet() else types
        return candidate.name != treatment.name && candidate.categories().contains(category) && candidate.types().containsAll(lookForTypes)
    }
}
