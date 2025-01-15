package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.createFullExpandedDoidTree
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.doid.DoidModel

class HasExhaustedSOCTreatments(
    private val recommendationEngineFactory: RecommendationEngineFactory, private val doidModel: DoidModel
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val recommendationEngine = recommendationEngineFactory.create()
        val isNSCLC = LUNG_NON_SMALL_CELL_CARCINOMA_DOID in createFullExpandedDoidTree(doidModel, record.tumor.doids)
        val hasReceivedPlatinumBasedDoubletOrMore =
            TreatmentFunctions.receivedPlatinumDoublet(record) || TreatmentFunctions.receivedPlatinumTripletOrAbove(record)
        val hasReceivedUndefinedChemoradiation = record.oncologicalHistory.any {
            it.treatments.flatMap { treatment -> treatment.categories() }
                .containsAll(listOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY)) &&
                    !it.hasTypeConfigured()
        }

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
                when {
                    hasReceivedPlatinumBasedDoubletOrMore -> EvaluationFactory.pass("SOC considered exhausted (platinum doublet in history)")

                    hasReceivedUndefinedChemoradiation -> EvaluationFactory.pass("SOC considered exhausted (chemoradiation in history)")

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