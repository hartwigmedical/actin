package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasCodeletionOfChromosomeArms(private val chromosomeArm1: String, private val chromosomeArm2: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Codeletion of chromosome arms $chromosomeArm1 and $chromosomeArm2 currently cannot be determined",
            "Undetermined codeletion of chromosome arms $chromosomeArm1 and $chromosomeArm2"
        )
    }
}