package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadCategoryAndTypesCombinedWithOtherCategoryAndTypes(
    private val category1: TreatmentCategory,
    private val types1: Set<TreatmentType>,
    private val category2: TreatmentCategory,
    private val types2: Set<TreatmentType>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hadCombination = hadCombination(record, false)
        val hadCombinationWithUnknownType = hadCombination(record, true)
        val hadCombinationWithTrialWithUnknownType = hadCombinationWithTrialWithUnknownType(record)
        val hadTrialWithUnspecifiedTreatment = record.oncologicalHistory.any { it.isTrial && it.allTreatments().isEmpty() }

        val treatmentDesc =
            "${concatItemsWithAnd(types1)} ${category1.display()} combined with ${concatItemsWithAnd(types2)} ${category2.display()}"

        return when {
            hadCombination -> {
                EvaluationFactory.pass("Has received $treatmentDesc")
            }

            hadCombinationWithUnknownType || hadCombinationWithTrialWithUnknownType || hadTrialWithUnspecifiedTreatment -> {
                EvaluationFactory.undetermined("Undetermined if received $treatmentDesc")
            }

            else -> {
                EvaluationFactory.fail("Has not received $treatmentDesc")
            }
        }
    }

    private fun containsCategoryOfTypes(
        record: PatientRecord,
        category: TreatmentCategory,
        types: Set<TreatmentType>,
        includeEmptyTypes: Boolean
    ): List<TreatmentHistoryEntry> {
        return record.oncologicalHistory.filter {
            it.allTreatments()
                .any { treatment ->
                    treatment.categories().contains(category) && (treatment.types()
                        .containsAll(types) || (includeEmptyTypes && treatment.types().isEmpty()))
                }
        }
    }

    private fun hadCombination(
        record: PatientRecord,
        includeUnknownTypes: Boolean
    ): Boolean {
        val containsCategory1OfTypes1 = containsCategoryOfTypes(record, category1, types1, includeUnknownTypes)
        val containsCategory2OfTypes2 = containsCategoryOfTypes(record, category2, types2, includeUnknownTypes)
        return containsCategory1OfTypes1.intersect(containsCategory2OfTypes2.toSet()).isNotEmpty()
    }

    private fun hadCombinationWithTrialWithUnknownType(record: PatientRecord): Boolean {
        return containsCategoryOfTypes(record, category1, types1, true).any {
            TrialFunctions.treatmentMayMatchAsTrial(it, category2)
        } || containsCategoryOfTypes(record, category2, types2, true).any {
            TrialFunctions.treatmentMayMatchAsTrial(it, category1)
        }
    }
}
