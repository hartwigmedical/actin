package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasExhaustedSOCTreatments(
    private val standardOfCareEvaluatorFactory: StandardOfCareEvaluatorFactory,
    private val doidModel: DoidModel,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val standardOfCareEvaluator = standardOfCareEvaluatorFactory.create()

        return when {
            standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(record) -> {
                val treatmentEvaluation = standardOfCareEvaluator.evaluateRequiredTreatments(record)
                val remainingNonOptionalTreatments = treatmentEvaluation.potentiallyEligibleTreatments()
                    .joinToString(", ") { it.treatmentCandidate.treatment.display() }
                when {
                    remainingNonOptionalTreatments.isEmpty() -> {
                        EvaluationFactory.pass(labels.hasExhaustedSocTreatmentsPass())
                    }

                    treatmentEvaluation.isMissingTreatmentsWithPotentialIntoleranceOnly() -> {
                        EvaluationFactory.warn(
                            labels.hasExhaustedSocTreatmentsWarnPotentialIntolerance(remainingNonOptionalTreatments)
                        )
                    }

                    treatmentEvaluation.isMissingMolecularResultForEvaluation() -> {
                        EvaluationFactory.warn(
                            labels.hasExhaustedSocTreatmentsWarnMissingMolecular(remainingNonOptionalTreatments),
                            isMissingMolecularResultForEvaluation = true
                        )
                    }

                    else -> {
                        EvaluationFactory.fail(labels.hasExhaustedSocTreatmentsFail(remainingNonOptionalTreatments))
                    }
                }
            }

            DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID) -> {
                val treatmentHistoryAnalysis = TreatmentHistoryAnalysis.create(record, ignoreCurativeNeoAdjuvantOrAdjuvant = true)
                when {
                    treatmentHistoryAnalysis.receivedPlatinumDoublet() || treatmentHistoryAnalysis.receivedPlatinumTripletOrAbove() -> {
                        EvaluationFactory.pass(labels.hasExhaustedSocTreatmentsPassPlatinumDoublet())
                    }

                    treatmentHistoryAnalysis.receivedUndefinedChemoradiation() -> {
                        EvaluationFactory.pass(labels.hasExhaustedSocTreatmentsPassChemoradiation())
                    }

                    treatmentHistoryAnalysis.receivedUndefinedChemoImmunotherapy() -> {
                        EvaluationFactory.pass(labels.hasExhaustedSocTreatmentsPassChemoImmunotherapy())
                    }

                    treatmentHistoryAnalysis.receivedUndefinedChemotherapy() -> {
                        EvaluationFactory.undetermined(labels.hasExhaustedSocTreatmentsUndeterminedChemotherapy())
                    }

                    else -> EvaluationFactory.warn(labels.hasExhaustedSocTreatmentsWarnNoPlatinumDoublet())
                }
            }

            record.oncologicalHistory.isEmpty() -> {
                EvaluationFactory.undetermined(labels.hasExhaustedSocTreatmentsUndeterminedNoHistory())
            }

            else -> EvaluationFactory.pass(labels.hasExhaustedSocTreatmentsPassAssumed())
        }
    }
}