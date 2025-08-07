package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecent(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>,
    private val months: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Undetermined if received ${category.display()} of only type(s)" +
                    "${Format.concatItemsWithOr(types)} for at least $months months as most recent line"
        )
    }
}