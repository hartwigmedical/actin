package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.datamodel.ReceptorType
import com.hartwig.actin.doid.DoidModel

class HasBreastCancerWithPositiveReceptorOfType(private val doidModel: DoidModel, private val receptorType: ReceptorType) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, tumorDoids)
        val isBreastCancer = DoidConstants.BREAST_CANCER_DOID in expandedDoidSet
        val targetPriorMolecularTests = record.priorMolecularTests.filter { it.item == receptorType.display() }
        val targetReceptorPositiveInDoids = expandedDoidSet.contains(POSITIVE_DOID_MOLECULAR_COMBINATION[receptorType])
        val targetReceptorNegativeInDoids = expandedDoidSet.contains(NEGATIVE_DOID_MOLECULAR_COMBINATION[receptorType])

        val testSummary = summarizeTests(targetPriorMolecularTests)
        val positiveArguments = TestResult.POSITIVE in testSummary || targetReceptorPositiveInDoids
        val negativeArguments = TestResult.NEGATIVE in testSummary || targetReceptorNegativeInDoids

        val targetReceptorIsPositive = when {
            positiveArguments && !negativeArguments -> true
            negativeArguments && !positiveArguments -> false
            else -> null
        }
        val specificArgumentsForStatusDeterminationMissing = !(positiveArguments || negativeArguments)
        val targetHer2AndErbb2Amplified = receptorType == ReceptorType.HER2 && geneIsAmplifiedForPatient("ERBB2", record)

        return when {
            tumorDoids.isNullOrEmpty() -> {
                EvaluationFactory.undetermined(
                    "Undetermined if $receptorType positive breast cancer since no tumor doids configured", "No tumor doids configured"
                )
            }

            !isBreastCancer -> EvaluationFactory.fail("Patient does not have breast cancer", "Tumor type")

            targetPriorMolecularTests.isEmpty() && specificArgumentsForStatusDeterminationMissing -> {
                return if (targetHer2AndErbb2Amplified) {
                    EvaluationFactory.undetermined(
                        "${receptorType.display()}-status undetermined (IHC data missing) but probably positive since ERBB2 amp present",
                        "${receptorType.display()}-status undetermined (IHC data missing) but probably positive since ERBB2 amp present"
                    )
                } else {
                    EvaluationFactory.undetermined(
                        "${receptorType.display()}-status unknown - data missing",
                        "${receptorType.display()}-status unknown"
                    )
                }
            }

            targetReceptorIsPositive == null && !specificArgumentsForStatusDeterminationMissing -> {
                EvaluationFactory.undetermined(
                    "${receptorType.display()}-status undetermined since DOID and/or IHC data inconsistent",
                    "Undetermined ${receptorType.display()}-status - DOID and/or IHC data inconsistent"
                )
            }

            targetReceptorIsPositive == true -> {
                EvaluationFactory.pass(
                    "Patient has ${receptorType.display()}-positive breast cancer",
                    "Has ${receptorType.display()}-positive breast cancer"
                )
            }

            targetReceptorIsPositive != true && targetHer2AndErbb2Amplified -> {
                EvaluationFactory.warn(
                    "Patient does not have ${receptorType.display()}-positive breast cancer based on DOIDS and/or prior molecular tests " +
                            "but status is undetermined since ERBB2 gene amp present",
                    "Undetermined if ${receptorType.display()}-positive breast cancer since DOID/IHC data inconsistent with ERBB2 gene amp"
                )
            }

            targetReceptorIsPositive != false && TestResult.BORDERLINE in testSummary -> {
                if (receptorType == ReceptorType.HER2) {
                    return EvaluationFactory.undetermined(
                        "Patient does not have ${receptorType.display()}-positive breast cancer but ${receptorType.display()}-score is " +
                                "2+ hence additional FISH may be useful",
                        "No ${receptorType.display()}-positive breast cancer - ${receptorType.display()}-FISH may be beneficial (score 2+)"
                    )
                } else {
                    return EvaluationFactory.warn(
                        "Patient has ${receptorType.display()}-positive breast cancer but clinical relevance unknown " +
                                "(${receptorType.display()}-score in range 1-10 percent)",
                        "Has ${receptorType.display()}-positive breast cancer but clinical relevance unknown " +
                                "since ${receptorType.display()}-score under 10%"
                    )
                }
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient does not have ${receptorType.display()}-positive breast cancer",
                    "No ${receptorType.display()}-positive breast cancer"
                )
            }
        }
    }

    private enum class TestResult {
        POSITIVE,
        NEGATIVE,
        BORDERLINE,
        UNKNOWN
    }

    private fun classifyPrOrErTest(test: PriorMolecularTest): TestResult {
        return classifyTest(test, "%", 1, 10, 100)
    }

    private fun classifyHer2Test(test: PriorMolecularTest): TestResult {
        return classifyTest(test, "+", 2, 3, 3)
    }

    private fun classifyTest(
        test: PriorMolecularTest, unit: String, negativeUpperBound: Int, positiveLowerBound: Int, positiveUpperBound: Int
    ): TestResult {
        val scoreValue = test.scoreValue?.toInt()
        return when {
            test.scoreText?.lowercase() == "negative" || (scoreValue in 0 until negativeUpperBound && test.scoreValueUnit == unit) -> {
                TestResult.NEGATIVE
            }

            test.scoreText?.lowercase() == "positive" ||
                    (scoreValue in positiveLowerBound..positiveUpperBound && test.scoreValueUnit == unit) -> {
                TestResult.POSITIVE
            }

            scoreValue in negativeUpperBound until positiveLowerBound && test.scoreValueUnit == unit -> {
                TestResult.BORDERLINE
            }

            else -> TestResult.UNKNOWN
        }
    }

    private fun summarizeTests(targetPriorMolecularTests: List<PriorMolecularTest>): Set<TestResult> {
        val classifier = when (receptorType) {
            ReceptorType.ER, ReceptorType.PR -> ::classifyPrOrErTest
            ReceptorType.HER2 -> ::classifyHer2Test
        }
        return targetPriorMolecularTests.map(classifier).toSet()
    }

    companion object {
        private val POSITIVE_DOID_MOLECULAR_COMBINATION = mapOf(
            ReceptorType.ER to DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID,
            ReceptorType.PR to DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID,
            ReceptorType.HER2 to DoidConstants.HER2_POSITIVE_BREAST_CANCER_DOID
        )
        private val NEGATIVE_DOID_MOLECULAR_COMBINATION = mapOf(
            ReceptorType.ER to DoidConstants.ESTROGEN_NEGATIVE_BREAST_CANCER_DOID,
            ReceptorType.PR to DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID,
            ReceptorType.HER2 to DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID
        )
    }
}