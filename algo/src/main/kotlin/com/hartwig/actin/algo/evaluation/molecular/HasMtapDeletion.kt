package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget

private const val MTAP = "MTAP"
private const val CDKN2A = "CDKN2A"

class HasMtapDeletion : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val mtapTested =
            record.molecularTests.any { test -> test.targetSpecification?.testsGene(MTAP) { it.contains(MolecularTestTarget.DELETION) } == true }
                    || IhcTestEvaluation.create(MTAP, record.ihcTests).filteredTests.isNotEmpty()

        return when {
            mtapTested -> {
                ProteinIsLostByIhc(MTAP).evaluate(record).takeIfPassOrWarn()
                    ?: GeneIsInactivated(MTAP, onlyDeletions = true).evaluate(record).takeIfPassOrWarn()
                    ?: GeneIsInactivated(MTAP, onlyDeletions = false).evaluate(record).takeIfPassOrWarn()?.let { evaluation ->
                        evaluation.copy(
                            result = EvaluationResult.WARN,
                            passMessages = emptySet(),
                            warnMessages = evaluation.passMessages.ifEmpty { evaluation.warnMessages }
                        )
                    }
                    ?: EvaluationFactory.fail("No $MTAP deletion")
            }

            isPassOrWarn(GeneIsInactivated(CDKN2A, onlyDeletions = true).evaluate(record)) -> {
                EvaluationFactory.warn(
                    "$MTAP deletion not tested but $CDKN2A deletion detected (highly correlated)",
                    setOf("Potential $MTAP deletion"),
                    isMissingMolecularResultForEvaluation = true
                )
            }

            else -> EvaluationFactory.undetermined("$MTAP deletion not tested", isMissingMolecularResultForEvaluation = true)
        }
    }

    private fun isPassOrWarn(evaluation: Evaluation) = evaluation.result in setOf(EvaluationResult.PASS, EvaluationResult.WARN)

    private fun Evaluation.takeIfPassOrWarn() = this.takeIf(::isPassOrWarn)
}