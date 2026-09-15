package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasExhaustedSOCTreatments(
    private val standardOfCareEvaluatorFactory: StandardOfCareEvaluatorFactory, private val doidModel: DoidModel
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
                        EvaluationFactory.pass("SOC is exhausted")
                    }

                    treatmentEvaluation.isMissingTreatmentsWithPotentialIntoleranceOnly() -> {
                        EvaluationFactory.warn(
                            "SOC is potentially exhausted - remaining options ($remainingNonOptionalTreatments) " +
                                    "possibly due to drug intolerance"
                        )
                    }

                    treatmentEvaluation.isMissingMolecularResultForEvaluation() -> {
                        EvaluationFactory.warn(
                            "SOC potentially not exhausted ($remainingNonOptionalTreatments) " +
                                    "but some corresponding molecular results are missing",
                            isMissingMolecularResultForEvaluation = true
                        )
                    }

                    else -> {
                        EvaluationFactory.fail(
                            "SOC is not exhausted (remaining options: $remainingNonOptionalTreatments)"
                        )
                    }
                }
            }

            DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID) -> {
                val treatmentHistoryAnalysis = TreatmentHistoryAnalysis.create(record, ignoreCurativeNeoAdjuvantOrAdjuvant = true)
                val messageStart = "SOC considered exhausted"
                when {
                    treatmentHistoryAnalysis.receivedPlatinumDoublet() || treatmentHistoryAnalysis.receivedPlatinumTripletOrAbove() -> {
                        EvaluationFactory.pass("$messageStart (platinum doublet in provided history)")
                    }

                    treatmentHistoryAnalysis.receivedUndefinedChemoradiation() -> {
                        EvaluationFactory.pass("$messageStart (chemoradiation in provided history)")
                    }

                    treatmentHistoryAnalysis.receivedUndefinedChemoImmunotherapy() -> {
                        EvaluationFactory.pass("$messageStart (chemo-immunotherapy in provided history)")
                    }

                    treatmentHistoryAnalysis.receivedUndefinedChemotherapy() -> {
                        EvaluationFactory.undetermined("Undetermined if SOC exhausted (undefined chemotherapy in provided history)")
                    }

                    else -> EvaluationFactory.warn("SOC potentially not exhausted (no platinum doublet in metastatic setting)")
                }
            }

            record.oncologicalHistory.isEmpty() -> {
                EvaluationFactory.undetermined("Exhaustion of SOC undetermined (no prior cancer treatment)")
            }

            else -> EvaluationFactory.pass("Assumed that SOC is exhausted (prior cancer treatment)")
        }
    }
}