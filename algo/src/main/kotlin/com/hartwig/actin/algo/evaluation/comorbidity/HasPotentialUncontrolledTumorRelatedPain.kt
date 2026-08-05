package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.evaluation.medication.medicationNotProvided
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasPotentialUncontrolledTumorRelatedPain(
    private val selector: MedicationSelector,
    private val severePainMedication: Set<AtcLevel>,
    private val icdModel: IcdModel,
    private val comorbidityLabels: EvaluationLabels.Comorbidity,
    private val medicationLabels: EvaluationLabels.Medication
) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (hasCancerRelatedPainConditionOrHistory, hasAcutePainConditionOrHistory) = listOf(
            IcdConstants.CHRONIC_CANCER_RELATED_PAIN_CODE,
            IcdConstants.ACUTE_PAIN_CODE
        ).map { code ->
            icdModel.findInstancesMatchingAnyIcdCode(record.comorbidities, setOf(IcdCode(code))).fullMatches.isNotEmpty()
        }

        val medications = record.medications ?: return medicationNotProvided(medicationLabels)
        val (activePainMedications, plannedPainMedications) = selector.extractActiveAndPlannedWithCategory(
            medications,
            severePainMedication
        )

        return when {
            hasCancerRelatedPainConditionOrHistory -> {
                EvaluationFactory.undetermined(comorbidityLabels.hasPotentialUncontrolledTumorRelatedPainUndeterminedHistory())
            }

            hasAcutePainConditionOrHistory -> {
                EvaluationFactory.undetermined(comorbidityLabels.hasPotentialUncontrolledTumorRelatedPainUndeterminedAcute())
            }

            activePainMedications.isNotEmpty() -> {
                EvaluationFactory.warn(
                    comorbidityLabels.hasPotentialUncontrolledTumorRelatedPainWarn(Format.concatLowercaseWithCommaAndAnd(activePainMedications))
                )
            }

            plannedPainMedications.isNotEmpty() -> {
                EvaluationFactory.warn(
                    comorbidityLabels.hasPotentialUncontrolledTumorRelatedPainWarnPlanned(
                        Format.concatLowercaseWithCommaAndAnd(plannedPainMedications)
                    )
                )
            }

            else -> {
                EvaluationFactory.fail(comorbidityLabels.hasPotentialUncontrolledTumorRelatedPainFail())
            }
        }
    }
}
