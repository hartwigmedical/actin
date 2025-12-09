package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.TestResult
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.classifyHer2Test
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.classifyPrOrErTest
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.doid.DoidModel

class HasBreastCancerWithPositiveReceptorOfType(
    private val doidModel: DoidModel, private val receptorType: ReceptorType
): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, tumorDoids)
        val isBreastCancer = DoidConstants.BREAST_CANCER_DOID in expandedDoidSet
        val targetMolecularTests = record.ihcTests.filter { it.item == receptorType.display() }
        val targetReceptorPositiveInDoids =
            expandedDoidSet.contains(BreastCancerReceptorFunctions.POSITIVE_DOID_MOLECULAR_COMBINATION[receptorType])
        val targetReceptorNegativeInDoids =
            expandedDoidSet.contains(BreastCancerReceptorFunctions.NEGATIVE_DOID_MOLECULAR_COMBINATION[receptorType])
                || expandedDoidSet.contains(DoidConstants.TRIPLE_NEGATIVE_BREAST_CANCER_DOID)

        val testSummary = BreastCancerReceptorFunctions.summarizeTests(targetMolecularTests, receptorType)
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
                EvaluationFactory.undetermined("Undetermined if $receptorType positive breast cancer (tumor doids missing)")
            }

            !isBreastCancer -> EvaluationFactory.fail("No breast cancer")

            targetMolecularTests.isEmpty() && specificArgumentsForStatusDeterminationMissing -> {
                return if (targetHer2AndErbb2Amplified) {
                    EvaluationFactory.undetermined(
                        "${receptorType.display()}-status undetermined (IHC data missing) but probably positive since ERBB2 amp present"
                    )
                } else {
                    EvaluationFactory.undetermined("${receptorType.display()}-status unknown (data missing)")
                }
            }

            targetReceptorIsPositive == null && !specificArgumentsForStatusDeterminationMissing -> {
                EvaluationFactory.undetermined("${receptorType.display()}-status undetermined (DOID and/or IHC data inconsistent)")
            }

            targetReceptorIsPositive == true -> {
                EvaluationFactory.pass("Has ${receptorType.display()}-positive breast cancer")
            }

            targetReceptorIsPositive != true && targetHer2AndErbb2Amplified -> {
                EvaluationFactory.warn(
                    "Undetermined if ${receptorType.display()}-positive breast cancer (DOID/IHC data inconsistent with ERBB2 gene amp)"
                )
            }

            targetReceptorIsPositive != false && TestResult.BORDERLINE in testSummary -> {
                if (receptorType == ReceptorType.HER2) {
                    return EvaluationFactory.undetermined(
                        "No ${receptorType.display()}-positive breast cancer but ${receptorType.display()}-score is" +
                                "2+ hence FISH may be useful"
                    )
                } else {
                    return EvaluationFactory.warn(
                        "Has ${receptorType.display()}-positive breast cancer but clinical relevance unknown " +
                                "(${receptorType.display()}-score under 10%)"
                    )
                }
            }

            else -> {
                EvaluationFactory.fail("No ${receptorType.display()}-positive breast cancer")
            }
        }
    }
}