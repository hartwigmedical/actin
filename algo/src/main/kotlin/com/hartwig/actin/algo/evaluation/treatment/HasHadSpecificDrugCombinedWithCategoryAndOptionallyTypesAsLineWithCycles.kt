package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineWithCycles(
    private val drugToFind: Drug,
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?,
    private val line: Int?,
    private val minCycles: Int?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val specificDrugCombinedWithCategoryAndTypesEvaluator =
            SpecificDrugCombinedWithCategoryAndTypesEvaluator(drugToFind, category, types)
        val relevantHistory = specificDrugCombinedWithCategoryAndTypesEvaluator.relevantHistory(record)

        val historyWithSpecificCombination = relevantHistory.filter { entry ->
            entry.allTreatments()
                .any { specificDrugCombinedWithCategoryAndTypesEvaluator.treatmentWithoutDrugMatchesCategoryAndType(it) }
        }
        val hasSufficientCycles = historyWithSpecificCombination.map { entry ->
            val cycles = entry.treatmentHistoryDetails?.cycles
            when {
                minCycles == null -> true
                cycles == null -> null
                else -> cycles >= minCycles
            }
        }.toSet()

        val hadCombinationWithTrialWithUnknownType =
            relevantHistory.any { TrialFunctions.treatmentMayMatchAsTrial(it, setOf(category)) }
        val hadTrialWithUnspecifiedTreatment =
            record.oncologicalHistory.any { it.isTrial && it.allTreatments().isEmpty() }

        val treatmentDesc = specificDrugCombinedWithCategoryAndTypesEvaluator.treatmentString()
        val treatmentDescStart = treatmentDesc.replaceFirstChar { it.uppercase() }
        val cyclesString = minCycles?.let { " and at least $it cycles" } ?: ""

        return when {
            historyWithSpecificCombination.isNotEmpty() && line != null -> {
                EvaluationFactory.undetermined("$treatmentDescStart in provided treatments but unknown if in line $line")
            }

            true in hasSufficientCycles -> EvaluationFactory.pass("$treatmentDescStart$cyclesString in provided treatments")

            false in hasSufficientCycles -> EvaluationFactory.warn("$treatmentDescStart in provided treatments but with less than $minCycles cycles")

            null in hasSufficientCycles -> EvaluationFactory.undetermined("Undetermined history of $treatmentDesc$cyclesString based on provided treatments")

            hadCombinationWithTrialWithUnknownType || hadTrialWithUnspecifiedTreatment -> {
                EvaluationFactory.undetermined("Undetermined history of $treatmentDesc$cyclesString based on provided treatments")
            }

            else -> EvaluationFactory.fail("No $treatmentDesc in provided treatments")
        }
    }
}
