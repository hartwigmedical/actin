package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class IsEligibleForTreatmentOfCategoryAndType(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>
): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.recoverableUndetermined(
            "Undetermined if patient is eligible for treatment of category $category and any type of ${types.joinToString(",")}"
        )
    }
}