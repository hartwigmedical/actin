package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasSufficientGeneToCentromereRatioByFish(private val gene: String, private val chromosome: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "$gene to centromere of chromosome $chromosome ratio by FISH undetermined",
            isMissingMolecularResultForEvaluation = true
        )
    }
}