package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class HasHadLimitedWeeksOfSpecificTreatment(private val treatmentToFind: Treatment, private val maxWeeks: Int?) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return TreatmentDurationEvaluator(
            { it.name == treatmentToFind.name },
            { it.treatments.isEmpty() },
            treatmentToFind.categories(),
            treatmentToFind.display().lowercase(),
            TreatmentDurationType.LIMITED,
            maxWeeks
        ).evaluate(record)
    }
}