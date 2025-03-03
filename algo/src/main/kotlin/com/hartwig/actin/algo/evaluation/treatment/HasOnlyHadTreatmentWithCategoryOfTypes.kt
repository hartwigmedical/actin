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
        val (matchingTreatments, diffTreatments) = treatments.partition {
            it.categories().contains(category) && it.types().intersect(types).isNotEmpty()
        }
        val (unknownType, wrongTreatments) = diffTreatments.partition {
            it.categories().contains(category) && it.types().isEmpty()
        }
        return when {
            wrongTreatments.isNotEmpty() -> {
                EvaluationFactory.fail("There are treatments of the wrong category or type")
            }
            unknownType.isNotEmpty() -> {
                EvaluationFactory.warn("There are treatments with unknown type")
            }
            matchingTreatments.isNotEmpty() -> {
                EvaluationFactory.pass("There are only treatments with the correct category and types")
            }
            else -> {
                EvaluationFactory.fail("No treatments found")
            }
        }
    }
}