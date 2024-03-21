package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

class HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypes(
    private val drugToFind: Drug,
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val relevantHistory = record.oncologicalHistory.filter { history ->
            history.allTreatments().any { pastTreatment -> pastTreatment.name.lowercase() == treatment.name.lowercase() }
        }

        val treatmentDesc =
            "combined therapy with $drugToFind and ${types?.let { concatItemsWithAnd(types) } ?: ""} ${category.display()}"

        if (historyMatchesCategoryAndTypes(relevantHistory)) {
            return EvaluationFactory.pass("Patient has received $treatmentDesc", "Has received $treatmentDesc")
        }

        if (historyMatchesCategoryAndTypes(record.oncologicalHistory)) {
            return EvaluationFactory.undetermined(
                "Undetermined if patient may have received $treatmentDesc",
                "Undetermined if received $treatmentDesc"
            )
        }
        return EvaluationFactory.fail("Patient has not received $treatmentDesc", "Has not received $treatmentDesc")
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
