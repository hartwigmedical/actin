package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLine(
    private val drugToFind: Drug,
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?,
    private val line: Int?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val specificDrugCombinedWithCategoryAndTypesEvaluator =
            SpecificDrugCombinedWithCategoryAndTypesEvaluator(drugToFind, category, types)
        val relevantHistory = specificDrugCombinedWithCategoryAndTypesEvaluator.relevantHistory(record)

        val hadSpecificCombination = relevantHistory.flatMap { it.allTreatments() }
            .any { treatment -> specificDrugCombinedWithCategoryAndTypesEvaluator.treatmentWithoutDrugMatchesCategoryAndType(treatment) }
        val hadCombinationWithTrialWithUnknownType = relevantHistory.any { TrialFunctions.treatmentMayMatchAsTrial(it, setOf(category)) }
        val hadTrialWithUnspecifiedTreatment = record.oncologicalHistory.any { it.isTrial && it.allTreatments().isEmpty() }

        val treatmentDesc = specificDrugCombinedWithCategoryAndTypesEvaluator.treatmentString()

        return when {
            hadSpecificCombination && line != null -> EvaluationFactory.undetermined("Has received $treatmentDesc but unknown if in line $line")

            hadSpecificCombination -> EvaluationFactory.pass("Has received $treatmentDesc")

            hadCombinationWithTrialWithUnknownType || hadTrialWithUnspecifiedTreatment -> {
                EvaluationFactory.undetermined("Undetermined if received $treatmentDesc")
            }

            else -> {
                EvaluationFactory.fail("Has not received $treatmentDesc")
            }
        }
    }
}
