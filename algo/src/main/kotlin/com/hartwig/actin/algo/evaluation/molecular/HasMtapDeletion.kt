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
            record.molecularTests.any { test -> test.targetSpecification?.testsGene(MTAP) { it == listOf(MolecularTestTarget.DELETION) } == true }
                    || IhcTestEvaluation.create(MTAP, record.ihcTests).filteredTests.isNotEmpty()

        return when {
            mtapTested -> {
                val mtapDeletion = GeneIsInactivated(MTAP, onlyDeletions = true).evaluate(record)
                val mtapIhcLoss = ProteinIsLostByIhc(MTAP).evaluate(record)
                val mtapInactivation = GeneIsInactivated(MTAP, onlyDeletions = false).evaluate(record)

                when {
                    isPassOrWarn(mtapIhcLoss) -> mtapIhcLoss

                    isPassOrWarn(mtapDeletion) -> mtapDeletion

                    isPassOrWarn(mtapInactivation) -> {
                        mtapInactivation.copy(
                            result = EvaluationResult.WARN,
                            passMessages = emptySet(),
                            warnMessages = mtapInactivation.passMessages.ifEmpty { mtapInactivation.warnMessages }
                        )
                    }

                    else -> EvaluationFactory.fail("No $MTAP deletion")
                }
            }

            else -> {
                val indirectEvaluation = GeneIsInactivated(CDKN2A, onlyDeletions = true).evaluate(record)
                when {
                    isPassOrWarn(indirectEvaluation) -> {
                        EvaluationFactory.warn(
                            "$MTAP deletion not tested but $CDKN2A deletion detected (highly correlated)",
                            setOf("Potential MTAP deletion"),
                            isMissingMolecularResultForEvaluation = true
                        )
                    }

                    else -> EvaluationFactory.undetermined("$MTAP deletion not tested", isMissingMolecularResultForEvaluation = true)
                }
            }
        }
    }

    private fun isPassOrWarn(evaluation: Evaluation) = evaluation.result in setOf(EvaluationResult.PASS, EvaluationResult.WARN)
}