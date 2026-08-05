package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecent(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>,
    private val months: Int,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            labels.hasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecentUndetermined(
                category.display(), Format.concatItemsWithOr(types), months
            )
        )
    }
}