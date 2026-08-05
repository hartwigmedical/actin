package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadLimitedWeeksOfTreatmentOfCategoryWithTypes(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>,
    private val maxWeeks: Int,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return TreatmentDurationEvaluator(
            { category in it.categories() && it.types().any(types::contains) },
            { category in it.categories() && !it.hasTypeConfigured() },
            setOf(category),
            labels.hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesTreatmentDescription(Format.concatItemsWithOr(types), category.display()),
            TreatmentDurationType.LIMITED,
            maxWeeks,
            labels
        ).evaluate(record)
    }
}
