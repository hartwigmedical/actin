package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
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
            history.allTreatments().any { (it as? DrugTreatment)?.drugs?.contains(drugToFind) == true }
        }

        val possiblyRelevantHistory = record.oncologicalHistory.filter { history ->
            history.allTreatments().any { (it as? DrugTreatment)?.drugs?.any {
                    drug -> drug.category == drugToFind.category && drug.name.contains("UNKNOWN", true)
            } == true } || history.allTreatments().isEmpty()
        }

        val treatmentDesc =
            "combined therapy with $drugToFind and ${types?.let { concatItemsWithAnd(types) } ?: ""} ${category.display()}"

        return if (historyMatchesCategoryAndTypes(relevantHistory)) {
            EvaluationFactory.pass("Patient has received $treatmentDesc", "Has received $treatmentDesc")
        } else if (historyMatchesCategoryAndTypes(possiblyRelevantHistory)) {
            EvaluationFactory.undetermined(
                "Undetermined if patient may have received $treatmentDesc",
                "Undetermined if received $treatmentDesc"
            )
        } else {
            EvaluationFactory.fail("Patient has not received $treatmentDesc", "Has not received $treatmentDesc")
        }
    }

    private fun historyMatchesCategoryAndTypes(treatmentHistory: List<TreatmentHistoryEntry>): Boolean {
        return treatmentHistory.any { treatmentLine ->
            treatmentLine.allTreatments().any { pastTreatment ->
                val treatmentWithoutDrug = (pastTreatment as? DrugTreatment)?.let { it.copy(drugs = it.drugs - drugToFind) } ?: pastTreatment
                treatmentWithoutDrug.categories().contains(category) && treatmentWithoutDrug.types()
                    .containsAll(types ?: emptySet())
            }
        }
    }
}
