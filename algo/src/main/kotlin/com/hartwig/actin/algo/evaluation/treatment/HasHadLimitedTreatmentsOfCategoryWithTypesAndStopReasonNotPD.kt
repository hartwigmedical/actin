package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

class HasHadLimitedTreatmentsOfCategoryWithTypesAndStopReasonNotPD(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>,
    private val maxWeeks: Int
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentMessage = "treatment with ${types.joinToString { it.display() }} ${category.display()} for at most $maxWeeks weeks " +
                "with stop reason other than PD"
        return EvaluationFactory.undetermined(
            "Undetermined if patient has had $treatmentMessage ",
            "Undetermined $treatmentMessage"
        )
    }

}