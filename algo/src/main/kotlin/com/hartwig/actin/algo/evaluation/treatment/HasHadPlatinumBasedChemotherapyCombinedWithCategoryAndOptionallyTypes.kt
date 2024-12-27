package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadPlatinumBasedChemotherapyCombinedWithCategoryAndOptionallyTypes(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val relevantHistory = record.oncologicalHistory.filter { entry ->
            val chemotherapyDrugs = TreatmentFunctions.createChemotherapyDrugList(entry)
            chemotherapyDrugs.any { it.drugTypes.contains(DrugType.PLATINUM_COMPOUND) }
        }

        val hadSpecificCombination = relevantHistory.any {
            it.allTreatments()
                .any { treatment -> treatment.categories().contains(category) && treatment.types().containsAll(types ?: emptySet()) }
        }
        val hadCombinationWithTrialWithUnknownType = relevantHistory.any { TrialFunctions.treatmentMayMatchAsTrial(it, category) }
        val hadTrialWithUnspecifiedTreatment = record.oncologicalHistory.any { it.isTrial && it.allTreatments().isEmpty() }

        val treatmentDesc =
            "platinum chemotherapy combined with ${types?.let { concatItemsWithAnd(types) } ?: ""}${category.display()}"

        return when {
            hadSpecificCombination -> {
                EvaluationFactory.pass("Patient has received $treatmentDesc", "Has received $treatmentDesc")
            }

            hadCombinationWithTrialWithUnknownType || hadTrialWithUnspecifiedTreatment -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient may have received $treatmentDesc",
                    "Undetermined if received $treatmentDesc"
                )
            }

            else -> {
                EvaluationFactory.fail("Patient has not received $treatmentDesc", "Has not received $treatmentDesc")
            }
        }
    }
}
