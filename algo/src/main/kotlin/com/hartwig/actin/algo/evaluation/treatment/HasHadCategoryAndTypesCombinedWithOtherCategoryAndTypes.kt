package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadCategoryAndTypesCombinedWithOtherCategoryAndTypes(
    private val firstCategory: TreatmentCategory,
    private val firstTypes: Set<TreatmentType>,
    private val secondCategory: TreatmentCategory,
    private val secondTypes: Set<TreatmentType>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val containsFirstCategoryOfTypes = record.oncologicalHistory.filter {
            it.allTreatments()
                .any { treatment -> treatment.categories().contains(firstCategory) && treatment.types().containsAll(firstTypes) }
        }

        val containsSecondCategoryOfTypes = record.oncologicalHistory.filter {
            it.allTreatments()
                .any { treatment -> treatment.categories().contains(secondCategory) && treatment.types().containsAll(secondTypes) }
        }

        val hadCombination = containsFirstCategoryOfTypes.intersect(containsSecondCategoryOfTypes.toSet()).isNotEmpty()

        val hadCombinationWithTrialWithUnknownType = containsFirstCategoryOfTypes.any {
            TrialFunctions.treatmentMayMatchAsTrial(
                it,
                secondCategory
            )
        } || containsSecondCategoryOfTypes.any { TrialFunctions.treatmentMayMatchAsTrial(it, firstCategory) }
        val hadTrialWithUnspecifiedTreatment = record.oncologicalHistory.any { it.isTrial && it.allTreatments().isEmpty() }

        val treatmentDesc =
            "${concatItemsWithAnd(firstTypes)} ${firstCategory.display()} combined with ${concatItemsWithAnd(secondTypes)} ${secondCategory.display()}"

        return when {
            hadCombination -> {
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
