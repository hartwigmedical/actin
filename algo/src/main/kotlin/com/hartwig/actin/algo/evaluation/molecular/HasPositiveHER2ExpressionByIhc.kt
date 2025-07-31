package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.TestResult
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.classifyHer2Test
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ReceptorType
import java.time.LocalDate

class HasPositiveHER2ExpressionByIhc(private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val receptorType = ReceptorType.HER2
        val (indeterminateIhcTests, validIhcTests) = IhcTestFilter.mostRecentAndUnknownDateIhcTestsForItem(record.ihcTests, receptorType.name)
            .partition(IhcTest::impliesPotentialIndeterminateStatus)
        val geneERBB2IsAmplified = geneIsAmplifiedForPatient("ERBB2", record, maxTestAge)

        val testResults = validIhcTests.map(::classifyHer2Test).toSet()

        val positiveArguments = TestResult.POSITIVE in testResults
        val negativeArguments = TestResult.NEGATIVE in testResults
        val her2ReceptorIsPositive = when {
            positiveArguments && !negativeArguments -> true
            negativeArguments && !positiveArguments -> false
            else -> null
        }

        val undeterminedMessage = "No IHC HER2 expression test available"

        return when {
            validIhcTests.isEmpty() && !(positiveArguments || negativeArguments) -> {
                return when {
                    geneERBB2IsAmplified -> {
                        EvaluationFactory.warn(
                            "$undeterminedMessage (but ERBB2 amplification detected)",
                            isMissingMolecularResultForEvaluation = true,
                            inclusionEvents = setOf("Potential IHC HER2 positive")
                        )
                    }

                    indeterminateIhcTests.isNotEmpty() -> {
                        EvaluationFactory.undetermined(
                            "$undeterminedMessage (indeterminate status)",
                            isMissingMolecularResultForEvaluation = true
                        )
                    }

                    else -> {
                        EvaluationFactory.undetermined(undeterminedMessage, isMissingMolecularResultForEvaluation = true)
                    }
                }
            }


            her2ReceptorIsPositive != true && geneERBB2IsAmplified -> {
                return if (her2ReceptorIsPositive == null) {
                    EvaluationFactory.undetermined(
                        "Non-positive HER2 IHC results (inconsistent with detected ERBB2 amplification)",
                        isMissingMolecularResultForEvaluation = true
                    )
                } else {
                    EvaluationFactory.undetermined(
                        "Negative HER2 IHC results (inconsistent with detected ERBB2 amplification)",
                        isMissingMolecularResultForEvaluation = true
                    )
                }
            }

            her2ReceptorIsPositive != false && TestResult.BORDERLINE in testResults -> {
                val results =
                    Format.concat(validIhcTests.filter { classifyHer2Test(it) == TestResult.BORDERLINE }.map { "${it.scoreValue}" })
                EvaluationFactory.warn(
                    "Undetermined if IHC HER2 score value(s) '$results' is considered positive",
                    inclusionEvents = setOf("Potential IHC HER2 positive")
                )
            }

            her2ReceptorIsPositive == null -> {
                return if (!(positiveArguments || negativeArguments)) {
                    EvaluationFactory.undetermined(
                        "IHC HER2 expression not deterministic by IHC",
                        isMissingMolecularResultForEvaluation = true
                    )
                } else {
                    EvaluationFactory.warn(
                        "Conflicting IHC HER2 expression test results",
                        inclusionEvents = setOf("Potential IHC HER2 expression")
                    )
                }
            }

            her2ReceptorIsPositive == true -> {
                EvaluationFactory.pass("IHC HER2 expression determined positive", inclusionEvents = setOf("IHC HER2 positive"))
            }

            her2ReceptorIsPositive == false -> EvaluationFactory.fail("IHC HER2 expression determined negative")

            else -> EvaluationFactory.undetermined("No positive IHC HER2 expression")
        }
    }
}