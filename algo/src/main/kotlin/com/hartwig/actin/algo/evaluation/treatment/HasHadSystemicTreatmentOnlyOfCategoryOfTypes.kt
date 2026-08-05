package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadSystemicTreatmentOnlyOfCategoryOfTypes(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentsByMatchEvaluation = record.oncologicalHistory.flatMap { it.allTreatments() }
            .filter { it.isSystemic }
            .groupBy {
                val matchesCategory = it.categories().contains(category)
                when {
                    matchesCategory && it.types().intersect(types).isNotEmpty() -> true
                    matchesCategory && it.types().isEmpty() -> null
                    else -> false
                }
            }

        val typesList = concatItemsWithOr(types)
        return when {
            false in treatmentsByMatchEvaluation -> {
                EvaluationFactory.fail(labels.hasHadSystemicTreatmentOnlyOfCategoryOfTypesFailNotOnly(typesList, category.display()))
            }

            null in treatmentsByMatchEvaluation -> {
                EvaluationFactory.undetermined(
                    labels.hasHadSystemicTreatmentOnlyOfCategoryOfTypesUndeterminedType(category.display(), typesList)
                )
            }

            record.oncologicalHistory.any { it.isTrial && it.allTreatments().isEmpty() } -> {
                EvaluationFactory.undetermined(
                    labels.hasHadSystemicTreatmentOnlyOfCategoryOfTypesUndeterminedTrial(typesList, category.display())
                )
            }

            true in treatmentsByMatchEvaluation -> {
                EvaluationFactory.pass(labels.hasHadSystemicTreatmentOnlyOfCategoryOfTypesPass(typesList, category.display()))
            }

            else -> {
                EvaluationFactory.fail(labels.hasHadSystemicTreatmentOnlyOfCategoryOfTypesFailNoPrior(typesList, category.display()))
            }
        }
    }
}