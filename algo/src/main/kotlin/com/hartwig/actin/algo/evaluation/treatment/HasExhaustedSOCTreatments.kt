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
                        EvaluationFactory.pass("Has exhausted SOC")
                    }
                    treatmentEvaluation.isMissingMolecularResultForEvaluation() -> {
                        EvaluationFactory.warn(
                            "Has potentially not exhausted SOC ($remainingNonOptionalTreatments) " +
                                    "but some corresponding molecular results are missing",
                            isMissingMolecularResultForEvaluation = true
                        )
                    }
                    else -> {
                        EvaluationFactory.fail(
                            "Has not exhausted SOC (remaining options: $remainingNonOptionalTreatments)"
                        )
                    }
                }
            }

            DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID) -> {
                val treatmentHistoryAnalysis = TreatmentHistoryAnalysis.create(record, ignoreCurativeNeoAdjuvantOrAdjuvant = true)
                val messageStart = "SOC considered exhausted"
                when {
                    treatmentHistoryAnalysis.receivedPlatinumDoublet() || treatmentHistoryAnalysis.receivedPlatinumTripletOrAbove() -> {
                        EvaluationFactory.pass("$messageStart (platinum doublet in history)")
                    }

                    treatmentHistoryAnalysis.receivedUndefinedChemoradiation() -> {
                        EvaluationFactory.pass("$messageStart (chemoradiation in history)")
                    }

                    treatmentHistoryAnalysis.receivedUndefinedChemoImmunotherapy() -> {
                        EvaluationFactory.pass("$messageStart (chemo-immunotherapy in history)")
                    }

                    treatmentHistoryAnalysis.receivedUndefinedChemotherapy() -> {
                        EvaluationFactory.undetermined("Undetermined if SOC exhausted (undefined chemotherapy in history)")
                    }

                    else -> EvaluationFactory.fail("Has not exhausted SOC (has not received platinum doublet in metastatic setting)")
                }
            }

            record.oncologicalHistory.isEmpty() -> {
                EvaluationFactory.undetermined("Exhaustion of SOC undetermined (no prior cancer treatment)")
            }

            else -> {
                EvaluationFactory.notEvaluated("Assumed that SOC is exhausted (had prior cancer treatment)")
            }
        }
    }
}