package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasReceivedSystemicTherapyForBrainMetastases(private val labels: EvaluationLabels.Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor
        val confirmedCnsOrBrainMetastases = tumorDetails.hasConfirmedBrainLesions() || tumorDetails.hasConfirmedCnsLesions()
        val suspectedCnsOrBrainMetastases = tumorDetails.hasSuspectedCnsLesions == true || tumorDetails.hasSuspectedBrainLesions == true
        val hasHadSystemicTreatment = SystemicTreatmentAnalyser.minSystemicTreatments(record.oncologicalHistory) > 0

        return if ((confirmedCnsOrBrainMetastases || suspectedCnsOrBrainMetastases) && hasHadSystemicTreatment) {
            val suspectedSuffix = if (!confirmedCnsOrBrainMetastases) {
                labels.hasReceivedSystemicTherapyForBrainMetastasesSuspectedSuffix()
            } else ""
            EvaluationFactory.warn(labels.hasReceivedSystemicTherapyForBrainMetastasesWarn(suspectedSuffix))
        } else {
            EvaluationFactory.fail(labels.hasReceivedSystemicTherapyForBrainMetastasesFail())
        }
    }
}