package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasRecentlyReceivedRadiotherapy internal constructor(private val referenceYear: Int, private val referenceMonth: Int) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasReceivedRadiotherapy = record.clinical().priorTumorTreatments()
            .filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }
            .any {
                val year = it.startYear()
                val month = it.startMonth()
                year == null || year == referenceYear && (month == null || month == referenceMonth)
            }
        return if (hasReceivedRadiotherapy) {
            EvaluationFactory.pass("Patient has recently received radiotherapy", "Has recently received radiotherapy")
        } else {
            EvaluationFactory.fail("Patient has not recently received radiotherapy", "No recent radiotherapy")
        }
    }
}