package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import java.time.LocalDate

class MmrStatusIsAvailable(maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge) {

    override fun noMolecularTestEvaluation() =
        EvaluationFactory.recoverableFail("No MMR status available (no molecular test)", isMissingMolecularResultForEvaluation = true)

    override fun evaluate(test: MolecularTest): Evaluation {
        return test.characteristics.microsatelliteStability?.let { EvaluationFactory.pass(message = "MMR status is available") }
            ?: EvaluationFactory.recoverableFail("No MMR status available", isMissingMolecularResultForEvaluation = true)
    }
}