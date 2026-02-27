package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadSufficientWeeksOfTreatmentOfCategoryWithTypes(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>,
    private val minWeeks: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return TreatmentDurationEvaluator(
            { category in it.categories() && it.types().any(types::contains) },
            { category in it.categories() && !it.hasTypeConfigured() },
            setOf(category),
            "${Format.concatItemsWithOr(types)} ${category.display()} treatment",
            TreatmentDurationType.SUFFICIENT,
            minWeeks
        ).evaluate(record)
    }
}