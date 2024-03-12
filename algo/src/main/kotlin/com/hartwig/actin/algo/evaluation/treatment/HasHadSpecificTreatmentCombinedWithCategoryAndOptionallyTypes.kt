package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

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

        if (historyMatchesCategoryAndTypes(relevantHistory)) {
            return EvaluationFactory.pass("Patient has received $treatmentDesc")
        }

        if (historyMatchesCategoryAndTypes(record.clinical.oncologicalHistory)) {
            return EvaluationFactory.undetermined(
                "Patient may have received $treatmentDesc during past trial participation",
                "Can't determine whether patient has received $treatmentDesc"
            )
        }
        return EvaluationFactory.fail("Patient has not received $treatmentDesc")
    }

    private fun historyMatchesCategoryAndTypes(treatmentHistory: List<TreatmentHistoryEntry>): Boolean {
        val lookForTypes = types ?: emptySet()
        return treatmentHistory.any { treatmentLine ->
            treatmentLine.allTreatments().any { pastTreatment ->
                pastTreatment.name != treatment.name && pastTreatment.categories().contains(category) && pastTreatment.types()
                    .containsAll(lookForTypes)
            }
        }
    }
}
