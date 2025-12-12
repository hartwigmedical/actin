package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget

class HasMtapDeletion : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val mtapTested =
            record.molecularTests.any { test -> test.targetSpecification?.testsGene("MTAP") { it == listOf(MolecularTestTarget.DELETION) } == true }
        val indirectEvaluation = GeneIsInactivated("CDKN2A", onlyDeletions = true).evaluate(record)

        return when {
            !mtapTested && indirectEvaluation.result in listOf(EvaluationResult.PASS, EvaluationResult.WARN) ->
                EvaluationFactory.warn("MTAP deletion not tested but CDKN2A deletion detected (highly correlated)")

            !mtapTested -> EvaluationFactory.undetermined("MTAP deletion not tested", isMissingMolecularResultForEvaluation = true)

            else -> GeneIsInactivated("MTAP", onlyDeletions = true).evaluate(record)
        }
    }
}