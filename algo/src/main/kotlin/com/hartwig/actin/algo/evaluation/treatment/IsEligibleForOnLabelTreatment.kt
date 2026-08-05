package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.composite.And
import com.hartwig.actin.algo.evaluation.composite.Not
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.evaluation.molecular.GeneHasActivatingMutation
import com.hartwig.actin.algo.evaluation.molecular.GeneHasVariantInExonRangeOfType
import com.hartwig.actin.algo.evaluation.molecular.GeneHasVariantWithProteinImpact
import com.hartwig.actin.algo.evaluation.molecular.HasMolecularDriverEventInNsclc
import com.hartwig.actin.algo.evaluation.molecular.HasSufficientPDL1ByIhc
import com.hartwig.actin.algo.evaluation.molecular.Pdl1Measure
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.trial.VariantTypeInput
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class IsEligibleForOnLabelTreatment(
    private val treatment: Treatment,
    private val standardOfCareEvaluatorFactory: StandardOfCareEvaluatorFactory,
    private val doidModel: DoidModel,
    private val minTreatmentDate: LocalDate,
    private val intent: Intent? = null,
    private val molecularLabels: EvaluationLabels.Molecular,
    private val treatmentLabels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val standardOfCareEvaluator = standardOfCareEvaluatorFactory.create()
        val treatmentDisplay = intent?.let { "${intent.name.lowercase()} ${treatment.display()}" } ?: treatment.display()
        val undeterminedMessage = treatmentLabels.isEligibleForOnLabelTreatmentUndetermined(treatmentDisplay)
        val isNsclc = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)

        return when {
            standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(record) -> {
                val potentiallyEligibleTreatments =
                    standardOfCareEvaluator.standardOfCareEvaluatedTreatments(record).potentiallyEligibleTreatments()
                if (potentiallyEligibleTreatments.any { it.treatmentCandidate.treatment.name.equals(treatment.name, ignoreCase = true) }) {
                    EvaluationFactory.undetermined(undeterminedMessage)
                } else {
                    EvaluationFactory.fail(treatmentLabels.isEligibleForOnLabelTreatmentFail(treatmentDisplay))
                }
            }

            isNsclc && treatmentNameToEvaluationFunctionsForNSCLC.containsKey(treatmentDisplay) -> {
                when (evaluate(record, treatmentNameToEvaluationFunctionsForNSCLC[treatmentDisplay]!!).result) {
                    EvaluationResult.PASS -> {
                        if (intent == null) {
                            EvaluationFactory.pass(treatmentLabels.isEligibleForOnLabelTreatmentPass(treatmentDisplay))
                        } else {
                            EvaluationFactory.undetermined(undeterminedMessage)
                        }
                    }

                    EvaluationResult.FAIL -> EvaluationFactory.fail(treatmentLabels.isEligibleForOnLabelTreatmentFail(treatmentDisplay))

                    else -> EvaluationFactory.undetermined(undeterminedMessage)
                }
            }

            record.oncologicalHistory.flatMap { it.allTreatments() }.any { it.name.equals(treatment.name, ignoreCase = true) } -> {
                EvaluationFactory.warn(treatmentLabels.isEligibleForOnLabelTreatmentWarn(treatmentDisplay))
            }

            else -> EvaluationFactory.undetermined(undeterminedMessage)
        }
    }

    private fun evaluate(record: PatientRecord, evaluationFunctions: EvaluationFunction): Evaluation {
        return And(
            listOf(
                evaluationFunctions,
                Not(
                    Or(
                        listOf(
                            HasHadSpecificTreatmentSinceDate(treatment, minTreatmentDate, treatmentLabels),
                            HasHadPDFollowingSpecificTreatment(listOf(treatment), treatmentLabels)
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
                        GeneHasActivatingMutation("EGFR", null, labels = molecularLabels),
                        Not(GeneHasVariantInExonRangeOfType("EGFR", 20, 20, VariantTypeInput.INSERT, molecularLabels))
                    )
                ),
                And(
                    listOf(
                        GeneHasVariantWithProteinImpact("EGFR", setOf("T790M"), molecularLabels),
                        HasHadSomeTreatmentsWithCategoryOfTypes(
                            TreatmentCategory.TARGETED_THERAPY,
                            setOf(DrugType.TYROSINE_KINASE_INHIBITOR_GEN_1, DrugType.TYROSINE_KINASE_INHIBITOR_GEN_2),
                            1,
                            treatmentLabels
                        )
                    )
                )
            )
        ),
        "Pembrolizumab" to PembrolizumabEvaluationFunction(doidModel, molecularLabels, treatmentLabels)
    )

    private class PembrolizumabEvaluationFunction(
        private val doidModel: DoidModel,
        private val molecularLabels: EvaluationLabels.Molecular,
        private val treatmentLabels: EvaluationLabels.Treatment
    ) : EvaluationFunction {
        override fun evaluate(record: PatientRecord): Evaluation {
            val isTreatmentNaive = HasHadLimitedSystemicTreatments(0, treatmentLabels).evaluate(record).result == EvaluationResult.PASS
            val egfrOrAlkDriverEvaluationResult = HasMolecularDriverEventInNsclc(
                setOf("EGFR", "ALK"),
                emptySet(),
                warnForMatchesOutsideGenesToInclude = false,
                withAvailableSoc = false,
                labels = molecularLabels
            ).evaluate(record).result
            val hasNoEgfrOrAlkDriver = egfrOrAlkDriverEvaluationResult == EvaluationResult.FAIL
            val hasEgfrOrAlkDriver = egfrOrAlkDriverEvaluationResult == EvaluationResult.PASS
            val hasPdl1Above50 =
                HasSufficientPDL1ByIhc(Pdl1Measure.TPS, 50.0, doidModel, molecularLabels).evaluate(record).result == EvaluationResult.PASS

            return when {
                isTreatmentNaive && hasNoEgfrOrAlkDriver && hasPdl1Above50 -> EvaluationFactory.pass("")
                isTreatmentNaive && hasEgfrOrAlkDriver -> EvaluationFactory.fail("")
                else -> EvaluationFactory.undetermined("")
            }
        }
    }
}
