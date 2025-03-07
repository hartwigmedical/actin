package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadSystemicTreatmentOnlyOfCategoryOfTypes(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>
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
                EvaluationFactory.fail("Did not only receive $typesList ${category.display()} treatment")
            }

            null in treatmentsByMatchEvaluation -> {
                EvaluationFactory.undetermined("Undetermined if received ${category.display()} is of type $typesList")
            }

            true in treatmentsByMatchEvaluation -> {
                EvaluationFactory.pass("Has only had $typesList ${category.display()} treatment")
            }

            else -> {
                EvaluationFactory.fail("Has not had $typesList ${category.display()} treatment (no prior systemic treatment)")
            }
        }
    }
}