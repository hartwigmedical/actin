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

        val drugMatches = record.oncologicalHistory
            .flatMap(TreatmentHistoryEntry::allTreatments)
            .flatMap { (it as? DrugTreatment)?.drugs ?: emptyList() }
            .filter { it == drugToFind }

        val treatmentDesc =
            "combined therapy with $drugToFind and ${types?.let { concatItemsWithAnd(types) } ?: ""} ${category.display()}"

        return if (containsTargetDrugAndTreatmentCombination(record.oncologicalHistory, drugMatches) == true) {
            EvaluationFactory.pass("Patient has received $treatmentDesc", "Has received $treatmentDesc")
        } else if (containsTargetDrugAndTreatmentCombination(record.oncologicalHistory, drugMatches) == null) {
            EvaluationFactory.undetermined(
                "Undetermined if patient may have received $treatmentDesc",
                "Undetermined if received $treatmentDesc"
            )
        } else {
            EvaluationFactory.fail("Patient has not received $treatmentDesc", "Has not received $treatmentDesc")
        }
    }

    private fun containsTargetDrugAndTreatmentCombination(treatmentHistory: List<TreatmentHistoryEntry>, drugMatches: List<Drug>): Boolean? {
        val lookForTypes = types ?: emptySet()
        val otherTreatments = treatmentHistory.flatMap { treatmentLine ->
            treatmentLine.allTreatments().filterNot { treatment -> (treatment as? DrugTreatment)?.drugs?.contains(drugToFind) == true }
        }
        val possibleMatches = treatmentHistory.any {
            it.treatments.any { treatment -> (treatment as? DrugTreatment)?.drugs?.contains(drugToFind) == true}
                    && (it.treatments.any { treatment -> (treatment as? DrugTreatment)?.drugs.isNullOrEmpty() }
                    || it.treatments.any { treatment -> treatment.categories().contains(category) && treatment.types().isEmpty() })
        }
        return when {
            drugMatches.isNotEmpty()
                    && otherTreatments.any { it.categories().contains(category) && it.types().containsAll(lookForTypes) } -> {
                        true
                    }
            drugMatches.isNotEmpty() && possibleMatches -> null
            else -> false
        }
    }
}
