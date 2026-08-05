package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class IsEligibleForRadiotherapy(private val bodyLocation: String? = null, private val labels: EvaluationLabels.Treatment) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val messageAddition = if (bodyLocation != null) labels.isEligibleForRadiotherapySuffixToLocation(bodyLocation.lowercase()) else ""
        return EvaluationFactory.undetermined(labels.isEligibleForRadiotherapyUndetermined(messageAddition))
    }
}