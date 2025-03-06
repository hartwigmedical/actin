package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasOnlyHadTreatmentWithCategoryOfTypes(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val treatments = record.oncologicalHistory.flatMap { it.allTreatments() }
        val treatmentsByMatchEvaluation = treatments.groupBy {
            val matchesCategory = it.categories().contains(category)
            when {
                matchesCategory && it.types().intersect(types).isNotEmpty() -> true
                matchesCategory && it.types().isEmpty() -> null
                else -> false
            }
        }
        return when {
            false in treatmentsByMatchEvaluation -> {
                EvaluationFactory.fail("There are treatments of the wrong category or type")
            }

            null in treatmentsByMatchEvaluation -> {
                EvaluationFactory.warn("There are treatments with unknown type")
            }

            true in treatmentsByMatchEvaluation -> {
                EvaluationFactory.pass("There are only treatments with the correct category and types")
            }

            else -> {
                EvaluationFactory.fail("No treatments found")
            }
        }
    }
}