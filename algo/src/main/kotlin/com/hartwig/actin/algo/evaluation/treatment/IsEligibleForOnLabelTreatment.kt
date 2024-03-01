package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.clinical.datamodel.TumorDetails

class IsEligibleForOnLabelTreatment(
    private val treatmentName: String,
    private val recommendationEngineFactory: RecommendationEngineFactory
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val recommendationEngine = recommendationEngineFactory.create()

        return when {
            tumorIsCUP(record.clinical.tumor) -> {
                EvaluationFactory.undetermined(
                    "Tumor type is CUP and eligibility for on-label treatment is therefore undetermined",
                    "Tumor type CUP hence eligibility for on-label treatment undetermined"
                )
            }

            recommendationEngine.standardOfCareCanBeEvaluatedForPatient(record) -> {
                if (recommendationEngine.standardOfCareEvaluatedTreatments(record)
                        .any { it.treatmentCandidate.treatment.name == treatmentName }
                ) {
                    EvaluationFactory.undetermined("Undetermined if patient is eligible for on-label treatment ${treatmentName.lowercase()}")
                } else {
                    EvaluationFactory.fail("Patient is not eligible for on-label treatment ${treatmentName.lowercase()}")
                }
            }

            else -> {
                if (record.clinical.oncologicalHistory.isEmpty()) {
                    EvaluationFactory.undetermined(
                        "Patient has not had any prior cancer treatments and therefore undetermined eligibility for on-label treatment",
                        "Undetermined eligibility for on-label treatment"
                    )
                } else {
                    EvaluationFactory.notEvaluated(
                        "Assumed no eligibility for on-label treatment since patient has had prior cancer treatment",
                        "Assumed no eligibility for on-label treatment"
                    )
                }
            }
        }
    }

    private fun tumorIsCUP(tumor: TumorDetails): Boolean {
        return tumor.primaryTumorLocation == "Unknown" && tumor.primaryTumorSubLocation == "CUP"
    }
}