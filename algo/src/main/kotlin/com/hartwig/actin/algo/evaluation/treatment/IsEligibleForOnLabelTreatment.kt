package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.composite.And
import com.hartwig.actin.algo.evaluation.composite.Not
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.evaluation.molecular.GeneHasActivatingMutation
import com.hartwig.actin.algo.evaluation.molecular.GeneHasVariantInExonRangeOfType
import com.hartwig.actin.algo.evaluation.molecular.GeneHasVariantWithProteinImpact
import com.hartwig.actin.algo.evaluation.molecular.HasMolecularDriverEventInNsclc
import com.hartwig.actin.algo.evaluation.molecular.HasSufficientPDL1ByIhc
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.evaluation.tumor.TumorEvaluationFunctions.hasCancerOfUnknownPrimary
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.trial.VariantTypeInput
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class IsEligibleForOnLabelTreatment(
    private val treatment: Treatment,
    private val standardOfCareEvaluatorFactory: StandardOfCareEvaluatorFactory,
    private val doidModel: DoidModel,
    private val minTreatmentDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val standardOfCareEvaluator = standardOfCareEvaluatorFactory.create()
        val treatmentDisplay = treatment.display()
        val isNsclc = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)

        return when {
            hasCancerOfUnknownPrimary(record.tumor.name) -> {
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

            isNsclc && treatmentNameToEvaluationFunctionsForNSCLC.containsKey(treatmentDisplay) -> {
                when (evaluate(record, treatmentNameToEvaluationFunctionsForNSCLC[treatmentDisplay]!!).result) {
                    EvaluationResult.PASS, EvaluationResult.NOT_EVALUATED -> {
                        EvaluationFactory.pass("Eligible for on-label treatment $treatmentDisplay")
                    }

                    EvaluationResult.FAIL -> {
                        EvaluationFactory.fail("Not eligible for on-label treatment $treatmentDisplay")
                    }

                    else -> {
                        EvaluationFactory.recoverableUndetermined(
                            "Undetermined if patient is eligible for on-label treatment $treatmentDisplay"
                        )
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

    private fun evaluate(record: PatientRecord, evaluationFunctions: EvaluationFunction): Evaluation {
        return And(
            listOf(
                evaluationFunctions,
                Not(
                    Or(
                        listOf(
                            HasHadSpecificTreatmentSinceDate(treatment, minTreatmentDate),
                            HasHadPDFollowingSpecificTreatment(listOf(treatment))
                        )
                    )
                )
            )
        ).evaluate(record)
    }

    private val treatmentNameToEvaluationFunctionsForNSCLC: Map<String, EvaluationFunction> = mapOf(
        "Osimertinib" to Or(
            listOf(
                And(
                    listOf(
                        GeneHasActivatingMutation("EGFR", null),
                        Not(GeneHasVariantInExonRangeOfType("EGFR", 20, 20, VariantTypeInput.INSERT))
                    )
                ),
                And(
                    listOf(
                        GeneHasVariantWithProteinImpact("EGFR", setOf("T790M")),
                        HasHadSomeTreatmentsWithCategoryOfTypes(
                            TreatmentCategory.TARGETED_THERAPY,
                            setOf(DrugType.TYROSINE_KINASE_INHIBITOR_GEN_1, DrugType.TYROSINE_KINASE_INHIBITOR_GEN_2),
                            1
                        )
                    )
                )
            )
        ),
        "Pembrolizumab" to PembrolizumabEvaluationFunction(doidModel)
    )

    private class PembrolizumabEvaluationFunction(private val doidModel: DoidModel) : EvaluationFunction {
        override fun evaluate(record: PatientRecord): Evaluation {
            val isTreatmentNaive = HasHadLimitedSystemicTreatments(0).evaluate(record).result.isPassOrNotEvaluated()
            val egfrOrAlkDriverEvaluationResult = HasMolecularDriverEventInNsclc(
                setOf("EGFR", "ALK"),
                emptySet(),
                warnForMatchesOutsideGenesToInclude = false,
                withAvailableSoc = false
            ).evaluate(record).result
            val hasNoEgfrOrAlkDriver = egfrOrAlkDriverEvaluationResult == EvaluationResult.FAIL
            val hasEgfrOrAlkDriver = egfrOrAlkDriverEvaluationResult.isPassOrNotEvaluated()
            val hasPdl1Above50 = HasSufficientPDL1ByIhc("TPS", 50.0, doidModel).evaluate(record).result.isPassOrNotEvaluated()

            return when {
                isTreatmentNaive && hasNoEgfrOrAlkDriver && hasPdl1Above50 -> EvaluationFactory.pass("")
                isTreatmentNaive && hasEgfrOrAlkDriver -> EvaluationFactory.fail("")
                else -> EvaluationFactory.undetermined("")
            }
        }

        private fun EvaluationResult.isPassOrNotEvaluated() = this == EvaluationResult.PASS || this == EvaluationResult.NOT_EVALUATED
    }
}
