package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypes(
    private val drugToFind: Drug,
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val relevantHistory = record.oncologicalHistory.filter { history ->
            history.allTreatments().any { (it as? DrugTreatment)?.drugs?.contains(drugToFind) == true }
        }

        val hadSpecificCombination = relevantHistory.any(::historyEntryWithoutDrugMatchesCategoryAndType)
        val hadCombinationWithTrialWithUnknownType = relevantHistory.any { TrialFunctions.treatmentMayMatchAsTrial(it, category) }
        val hadTrialWithUnspecifiedTreatment = record.oncologicalHistory.any { it.isTrial && it.allTreatments().isEmpty() }

        val treatmentDesc =
            "combined therapy with ${drugToFind.display()} and ${types?.let { concatItemsWithAnd(types) } ?: ""}${category.display()}"

        return when {
            hadSpecificCombination -> {
                EvaluationFactory.pass("Has received $treatmentDesc")
            }

            hadCombinationWithTrialWithUnknownType || hadTrialWithUnspecifiedTreatment -> {
                EvaluationFactory.undetermined("Undetermined if received $treatmentDesc")
            }

            else -> {
                EvaluationFactory.fail("Has not received $treatmentDesc")
            }
        }
    }

    private fun historyEntryWithoutDrugMatchesCategoryAndType(treatmentLine: TreatmentHistoryEntry) =
        treatmentLine.allTreatments().any { pastTreatment ->
            val treatmentWithoutDrug = (pastTreatment as? DrugTreatment)?.let { it.copy(drugs = it.drugs - drugToFind) }
                ?: pastTreatment
            treatmentWithoutDrug.categories().contains(category) && treatmentWithoutDrug.types().containsAll(types ?: emptySet())
        }
}
