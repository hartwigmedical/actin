package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.doid.DoidModel

class HasBreastCancerWithPositiveReceptorOfType(
    private val doidModel: DoidModel, private val receptorType: ReceptorType
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Undetermined if $receptorType positive breast cancer (tumor doids missing)")
        }

        val breastCancerReceptorEvaluation = BreastCancerReceptorsEvaluator(doidModel).evaluate(tumorDoids!!, record.ihcTests, receptorType)
        val targetHer2AndErbb2Amplified = receptorType == ReceptorType.HER2 && geneIsAmplifiedForPatient("ERBB2", record)

        val warnInclusionEvents = setOf("Potential IHC ${receptorType.display()} positive")

        return when (breastCancerReceptorEvaluation) {
            BreastCancerReceptorEvaluation.NOT_BREAST_CANCER -> EvaluationFactory.fail("No breast cancer")

            BreastCancerReceptorEvaluation.DATA_MISSING -> {
                return if (targetHer2AndErbb2Amplified) {
                    EvaluationFactory.undetermined(
                        "${receptorType.display()}-status undetermined (IHC data missing) but probably positive since ERBB2 amp present"
                    )
                } else {
                    EvaluationFactory.undetermined("${receptorType.display()}-status unknown (data missing)")
                }
            }

            BreastCancerReceptorEvaluation.INCONSISTENT_DATA -> {
                EvaluationFactory.undetermined("${receptorType.display()}-status undetermined (DOID and/or IHC data inconsistent)")
            }

            BreastCancerReceptorEvaluation.POSITIVE -> {
                EvaluationFactory.pass(
                    "Has ${receptorType.display()}-positive breast cancer",
                    inclusionEvents = setOf("IHC ${receptorType.display()} positive")
                )
            }

            BreastCancerReceptorEvaluation.BORDERLINE -> {
                EvaluationFactory.undetermined(
                    "No ${receptorType.display()}-positive breast cancer but ${receptorType.display()}-score is " +
                            "2+ hence FISH may be useful",
                    isMissingMolecularResultForEvaluation = true
                )
            }

            BreastCancerReceptorEvaluation.LOW -> {
                return when {
                    targetHer2AndErbb2Amplified -> {
                        EvaluationFactory.warn(
                            "Undetermined if ${receptorType.display()}-positive breast cancer " +
                                    "(HER2 low IHC inconsistent with ERBB2 gene amp)",
                            inclusionEvents = warnInclusionEvents
                        )
                    }

                    receptorType == ReceptorType.HER2 -> {
                        EvaluationFactory.fail(
                            "No ${receptorType.display()}-positive breast cancer"
                        )
                    }

                    else -> {
                        EvaluationFactory.warn(
                            "Has ${receptorType.display()}-positive breast cancer but clinical relevance unknown " +
                                    "(${receptorType.display()}-score under 10%)",
                            inclusionEvents = warnInclusionEvents
                        )
                    }
                }
            }

            BreastCancerReceptorEvaluation.NEGATIVE -> {
                return if (targetHer2AndErbb2Amplified) {
                    EvaluationFactory.warn(
                        "Undetermined if ${receptorType.display()}-positive breast cancer (DOID/IHC data inconsistent with ERBB2 gene amp)",
                        inclusionEvents = warnInclusionEvents
                    )
                } else {
                    EvaluationFactory.fail("No ${receptorType.display()}-positive breast cancer")
                }
            }
        }
    }
}