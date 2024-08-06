package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.clinical.datamodel.ReceptorType

class HasPositiveHER2ExpressionByIHC: EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val receptorType = ReceptorType.HER2
        val positiveValue = 3
        val indeterminateValue = 2
        val upperValue = 4
        val targetPriorMolecularTests =
            record.priorIHCTests.filter { it.item == receptorType.display() }
        val geneERBB2IsAmplified = geneIsAmplifiedForPatient("ERBB2", record)

        val testResults = targetPriorMolecularTests.map {
            val scoreValue = it.scoreValue?.toInt()
            when {
                it.impliesPotentialIndeterminateStatus -> TestResult.UNKNOWN

                ((scoreValue in 0 until indeterminateValue && it.scoreValueUnit == "+") ||
                        it.scoreText?.lowercase() == "negative") -> TestResult.NEGATIVE

                ((scoreValue in positiveValue .. upperValue && it.scoreValueUnit == "+")) ||
                        it.scoreText?.lowercase() == "positive" -> TestResult.POSITIVE

                (scoreValue in indeterminateValue until upperValue && it.scoreValueUnit == "+") ->
                    TestResult.BORDERLINE

                else -> TestResult.UNKNOWN
            }
        }

        val positiveArguments = TestResult.POSITIVE in testResults
        val negativeArguments = TestResult.NEGATIVE in testResults
        val her2ReceptorIsPositive = when {
            positiveArguments && !negativeArguments -> true
            negativeArguments && !positiveArguments -> false
            else -> null
        }

        return when {
            targetPriorMolecularTests.isEmpty() && !(positiveArguments || negativeArguments) -> {
                return if (geneERBB2IsAmplified) {
                    EvaluationFactory.undetermined(
                        "No (reliable) HER2 expression test by IHC available but probably positive since ERBB2 amp present",
                        "HER2 expression not tested by IHC but probably positive since ERBB2 amp present"
                    )
                } else {
                    EvaluationFactory.undetermined(
                        "No (reliable) HER2 expression test by IHC available",
                        "HER2 expression not tested by IHC"
                    )
                }
            }

            her2ReceptorIsPositive != true && geneERBB2IsAmplified -> {
                return if (her2ReceptorIsPositive == null) {
                EvaluationFactory.warn(
                    "Patient does not have HER2 positive IHC, but status is undetermined since ERBB2 amp present",
                    "Non-positive HER2 IHC results inconsistent with ERBB2 amp"
                )
                } else {
                    EvaluationFactory.warn(
                        "Patient has HER2 negative IHC, but status is undetermined since ERBB2 amp present",
                        "Negative HER2 IHC results inconsistent with ERBB2 amp"
                    )
                }
            }

            her2ReceptorIsPositive != false && TestResult.BORDERLINE in testResults -> {
                EvaluationFactory.undetermined(
                    "HER2 expression IHC was borderline, consider ordering additional tests",
                    "HER2 expression by IHC was borderline, additional tests should be considered"
                )
            }

            her2ReceptorIsPositive == null -> {
                return if (!(positiveArguments || negativeArguments)) {
                    EvaluationFactory.undetermined(
                        "No (reliable) HER2 expression test by IHC available",
                        "HER2 expression not deterministic by IHC"
                    )
                } else {
                    EvaluationFactory.undetermined(
                        "Conflicting HER2 expression tests by IHC",
                        "Conflicting HER2 expression tests by IHC"
                    )
                }
            }

            her2ReceptorIsPositive == true -> {
                EvaluationFactory.pass(
                    "IHC HER2 expression determined positive",
                    "Positive HER2 expression determined by IHC"
                )
            }

            her2ReceptorIsPositive == false -> {
                EvaluationFactory.fail(
                    "HER2 expression determined negative with IHC",
                    "HER2 expression determined negative with IHC"
                )
            }

            else -> EvaluationFactory.undetermined(
                "Patient does not have positive HER2 expression determined by IHC",
                "No positive IHC HER2 expression"
            )

        }
    }

    private enum class TestResult {
        POSITIVE,
        NEGATIVE,
        BORDERLINE,
        UNKNOWN
    }
}