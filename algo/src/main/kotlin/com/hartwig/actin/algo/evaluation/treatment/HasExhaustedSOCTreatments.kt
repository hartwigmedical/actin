package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.createFullExpandedDoidTree
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasExhaustedSOCTreatments(
    private val recommendationEngineFactory: RecommendationEngineFactory, private val doidModel: DoidModel
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val recommendationEngine = recommendationEngineFactory.create()
        val isNSCLC = LUNG_NON_SMALL_CELL_CARCINOMA_DOID in createFullExpandedDoidTree(doidModel, record.tumor.doids)
        val hasReceivedPlatinumBasedDoubletOrMore =
            TreatmentFunctions.receivedPlatinumDoublet(record) || TreatmentFunctions.receivedPlatinumTripletOrAbove(record)

        return when {
            recommendationEngine.standardOfCareCanBeEvaluatedForPatient(record) -> {
                val remainingNonOptionalTreatments = recommendationEngine.determineRequiredTreatments(record)
                    .joinToString(", ") { it.treatmentCandidate.treatment.name.lowercase() }
                if (remainingNonOptionalTreatments.isEmpty()) {
                    EvaluationFactory.pass("Has exhausted SOC")
                } else {
                    EvaluationFactory.fail(
                        "Has not exhausted SOC (remaining options: $remainingNonOptionalTreatments)"
                    )
                }
            }

            isNSCLC -> {
                if (hasReceivedPlatinumBasedDoubletOrMore) {
                    EvaluationFactory.pass("SOC considered exhausted (platinum doublet in history)")
                } else EvaluationFactory.fail("SOC not exhausted (at least platinum doublet remaining)")
            }

            record.oncologicalHistory.isEmpty() -> {
                EvaluationFactory.undetermined("Undetermined exhaustion of SOC (no prior cancer treatment)")
            }

            else -> {
                EvaluationFactory.notEvaluated("Assumed exhaustion of SOC (had prior cancer treatment)")
            }
        }
    }
}