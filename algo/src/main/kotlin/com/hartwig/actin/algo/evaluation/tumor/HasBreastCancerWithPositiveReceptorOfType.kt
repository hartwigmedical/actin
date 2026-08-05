package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.doid.DoidModel

class HasBreastCancerWithPositiveReceptorOfType(
    private val doidModel: DoidModel,
    private val receptorType: ReceptorType,
    private val molecularLabels: EvaluationLabels.Molecular,
    private val tumorLabels: EvaluationLabels.Tumor
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(
                tumorLabels.hasBreastCancerWithPositiveReceptorOfTypeUndeterminedDoidsMissing(receptorType.toString())
            )
        }

        val breastCancerReceptorEvaluation = BreastCancerReceptorsEvaluator(doidModel).evaluate(tumorDoids!!, record.ihcTests, receptorType)
        val targetHer2AndErbb2Amplified = receptorType == ReceptorType.HER2 && geneIsAmplifiedForPatient("ERBB2", record, molecularLabels)

        val warnInclusionEvents = setOf("Potential IHC ${receptorType.display()} positive")

        return when (breastCancerReceptorEvaluation) {
            BreastCancerReceptorEvaluation.NOT_BREAST_CANCER ->
                EvaluationFactory.fail(tumorLabels.hasBreastCancerWithPositiveReceptorOfTypeFailNotBreastCancer())

            BreastCancerReceptorEvaluation.DATA_MISSING -> {
                if (targetHer2AndErbb2Amplified) {
                    EvaluationFactory.undetermined(
                        tumorLabels.hasBreastCancerWithPositiveReceptorOfTypeUndeterminedDataMissingHer2Amp(receptorType.display())
                    )
                } else {
                    EvaluationFactory.undetermined(
                        tumorLabels.hasBreastCancerWithPositiveReceptorOfTypeUndeterminedDataMissing(receptorType.display())
                    )
                }
            }

            BreastCancerReceptorEvaluation.INCONSISTENT_DATA -> {
                EvaluationFactory.undetermined(
                    tumorLabels.hasBreastCancerWithPositiveReceptorOfTypeUndeterminedInconsistentData(receptorType.display())
                )
            }

            BreastCancerReceptorEvaluation.POSITIVE -> {
                EvaluationFactory.pass(
                    tumorLabels.hasBreastCancerWithPositiveReceptorOfTypePass(receptorType.display()),
                    inclusionEvents = setOf("IHC ${receptorType.display()} positive")
                )
            }

            BreastCancerReceptorEvaluation.BORDERLINE -> {
                EvaluationFactory.undetermined(
                    tumorLabels.hasBreastCancerWithPositiveReceptorOfTypeUndeterminedBorderline(receptorType.display()),
                    isMissingMolecularResultForEvaluation = true
                )
            }

            BreastCancerReceptorEvaluation.LOW -> {
                when {
                    targetHer2AndErbb2Amplified -> {
                        EvaluationFactory.warn(
                            tumorLabels.hasBreastCancerWithPositiveReceptorOfTypeWarnLowHer2Amp(receptorType.display()),
                            inclusionEvents = warnInclusionEvents
                        )
                    }

                    receptorType == ReceptorType.HER2 -> {
                        EvaluationFactory.fail(
                            tumorLabels.hasBreastCancerWithPositiveReceptorOfTypeFail(receptorType.display())
                        )
                    }

                    else -> {
                        EvaluationFactory.warn(
                            tumorLabels.hasBreastCancerWithPositiveReceptorOfTypeWarnLowClinicalRelevanceUnknown(receptorType.display()),
                            inclusionEvents = warnInclusionEvents
                        )
                    }
                }
            }

            BreastCancerReceptorEvaluation.NEGATIVE -> {
                if (targetHer2AndErbb2Amplified) {
                    EvaluationFactory.warn(
                        tumorLabels.hasBreastCancerWithPositiveReceptorOfTypeWarnNegativeHer2Amp(receptorType.display()),
                        inclusionEvents = warnInclusionEvents
                    )
                } else {
                    EvaluationFactory.fail(tumorLabels.hasBreastCancerWithPositiveReceptorOfTypeFail(receptorType.display()))
                }
            }
        }
    }
}