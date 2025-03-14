package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.evaluation.molecular.GeneHasVariantInExonRangeOfType
import com.hartwig.actin.algo.evaluation.molecular.GeneHasVariantWithProteinImpact
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput
import java.time.LocalDate

class IsEligibleForOnLabelTreatment(
    private val treatment: Treatment,
    private val standardOfCareEvaluatorFactory: StandardOfCareEvaluatorFactory,
    private val doidModel: DoidModel,
    maxTestAge: LocalDate? = null
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val standardOfCareEvaluator = standardOfCareEvaluatorFactory.create()
        val treatmentDisplay = treatment.display()
        val isNSCLC = DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID in DoidEvaluationFunctions.createFullExpandedDoidTree(
            doidModel,
            record.tumor.doids
        )

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

            isNSCLC && treatmentNameToEvaluationFunctionsForNSCLC.containsKey(treatmentDisplay) -> {
                when (evaluate(record, treatmentNameToEvaluationFunctionsForNSCLC[treatmentDisplay]!!).result) {
                    EvaluationResult.PASS -> {
                        EvaluationFactory.pass("Eligible for on-label treatment $treatmentDisplay")
                    }

                    EvaluationResult.WARN -> {
                        EvaluationFactory.undetermined("Undetermined if patient is eligible for on-label treatment $treatmentDisplay")
                    }

                    else -> {
                        EvaluationFactory.fail("Not eligible for on-label treatment $treatmentDisplay")
                    }
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

    private fun evaluate(record: PatientRecord, evaluationFunctions: List<EvaluationFunction>): Evaluation {
        val evaluation = Or(evaluationFunctions).evaluate(record)
        return evaluation.copy(
            inclusionMolecularEvents = emptySet(),
            exclusionMolecularEvents = emptySet(),
            isMissingMolecularResultForEvaluation = evaluation.isMissingMolecularResultForEvaluation
        )
    }

    private val treatmentNameToEvaluationFunctionsForNSCLC: Map<String, List<EvaluationFunction>> = mapOf(
        "Osimertinib" to listOf(
            GeneHasVariantInExonRangeOfType("EGFR", 19, 19, VariantTypeInput.DELETE, maxTestAge),
            GeneHasVariantWithProteinImpact("EGFR", setOf("L858R"), maxTestAge)
        )
    )
}