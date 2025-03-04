package com.hartwig.actin.algo.evaluation.treatcment

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
        val treatmentMatch = treatments.groupBy {
            when {
                it.categories().contains(category) && it.types().intersect(types).isNotEmpty() -> true
                it.categories().contains(category) && it.types().isEmpty() -> null
                else -> false
            }
        }
        return when {
            !treatmentMatch[false].isNullOrEmpty() -> {
                EvaluationFactory.fail("There are treatments of the wrong category or type")
            }

            !treatmentMatch[null].isNullOrEmpty() -> {
                EvaluationFactory.warn("There are treatments with unknown type")
            }

            !treatmentMatch[true].isNullOrEmpty() -> {
                EvaluationFactory.pass("There are only treatments with the correct category and types")
            }

            else -> {
                EvaluationFactory.fail("No treatments found")
            }
        }
    }
}