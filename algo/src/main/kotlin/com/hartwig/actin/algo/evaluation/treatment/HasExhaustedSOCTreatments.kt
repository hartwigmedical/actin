package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasExhaustedSOCTreatments(
    private val standardOfCareEvaluatorFactory: StandardOfCareEvaluatorFactory, private val doidModel: DoidModel
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val standardOfCareEvaluator = standardOfCareEvaluatorFactory.create()
        val treatmentHistoryAnalyzer = TreatmentHistoryAnalyzer(record, doidModel)

        return when {
            standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(record) -> {
                val treatmentEvaluation = standardOfCareEvaluator.evaluateRequiredTreatments(record)
                val remainingNonOptionalTreatments = treatmentEvaluation.potentiallyEligibleTreatments()
                    .joinToString(", ") { it.treatmentCandidate.treatment.name.lowercase() }
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

            treatmentHistoryAnalyzer.isNsclc -> {
                when {
                    treatmentHistoryAnalyzer.receivedPlatinumDoublet() || treatmentHistoryAnalyzer.receivedPlatinumTripletOrAbove() -> {
                        EvaluationFactory.pass("SOC considered exhausted (platinum doublet in history)")
                    }

                    treatmentHistoryAnalyzer.receivedUndefinedChemoradiation() -> {
                        EvaluationFactory.pass("SOC considered exhausted (chemoradiation in history)")
                    }

                    else -> EvaluationFactory.fail("Has not exhausted SOC (at least platinum doublet remaining)")
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