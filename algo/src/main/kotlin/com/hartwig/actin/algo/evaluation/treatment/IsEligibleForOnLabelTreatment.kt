package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class IsEligibleForOnLabelTreatment(
    private val treatment: Treatment,
    private val standardOfCareEvaluatorFactory: StandardOfCareEvaluatorFactory
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val standardOfCareEvaluator = standardOfCareEvaluatorFactory.create()
        val treatmentDisplay = treatment.display()

        return when {
            tumorIsCUP(record.tumor) -> {
                EvaluationFactory.undetermined("Tumor type CUP hence eligibility for on-label treatment $treatmentDisplay undetermined")
            }

            standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(record) -> {
                val potentiallyEligibleTreatments =
                    standardOfCareEvaluator.standardOfCareEvaluatedTreatments(record).potentiallyEligibleTreatments()
                if (potentiallyEligibleTreatments.any { it.treatmentCandidate.treatment.name.equals(treatment.name, ignoreCase = true) }) {
                    EvaluationFactory.undetermined("Undetermined if patient is eligible for on-label treatment $treatmentDisplay")
                } else {
                    EvaluationFactory.fail("Not eligible for on-label treatment $treatmentDisplay")
                }
            }

            record.oncologicalHistory.flatMap { it.allTreatments() }.any { it.name.equals(treatment.name, ignoreCase = true) } -> {
                EvaluationFactory.warn(
                    "Patient might be ineligible for on-label $treatmentDisplay since this treatment was already administered"
                )
            }

            else -> {
                EvaluationFactory.undetermined("Undetermined if patient is eligible for on-label $treatmentDisplay")
            }
        }
    }

    private fun tumorIsCUP(tumor: TumorDetails): Boolean {
        return tumor.primaryTumorLocation == "Unknown" && tumor.primaryTumorSubLocation == "CUP"
    }
}