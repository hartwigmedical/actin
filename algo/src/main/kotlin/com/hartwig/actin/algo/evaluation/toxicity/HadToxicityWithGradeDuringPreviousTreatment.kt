package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HadToxicityWithGradeDuringPreviousTreatment(
    private val toxicityName: String,
    private val grade: Int,
    private val labels: EvaluationLabels.Toxicity
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(labels.hadToxicityWithGradeDuringPreviousTreatmentUndetermined(toxicityName, grade))
    }
}