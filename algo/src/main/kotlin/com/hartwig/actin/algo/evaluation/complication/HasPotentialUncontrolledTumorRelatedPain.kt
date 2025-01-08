package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.IcdCode

class HasPotentialUncontrolledTumorRelatedPain(
    private val selector: MedicationSelector,
    private val severePainMedication: Set<AtcLevel>,
    private val icdModel: IcdModel
) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (hasCancerRelatedPainComplicationOrHistory, hasAcutePainComplicationOrHistory) = listOf(
            IcdConstants.CHRONIC_CANCER_RELATED_PAIN_CODE,
            IcdConstants.ACUTE_PAIN_CODE
        ).map { code ->
            icdModel.findInstancesMatchingAnyIcdCode(
                OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions) + (record.complications ?: emptyList()),
                setOf(IcdCode(code))
            ).fullMatches.isNotEmpty()
        }

        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val (activePainMedications, plannedPainMedications) = selector.extractActiveAndPlannedWithCategory(
            medications,
            severePainMedication
        )

        return when {
            hasCancerRelatedPainComplicationOrHistory -> {
                EvaluationFactory.undetermined("Has tumor related pain in history - undetermined if uncontrolled")
            }

            hasAcutePainComplicationOrHistory -> {
                EvaluationFactory.undetermined("Has acute pain in history - undetermined if uncontrolled")
            }

            activePainMedications.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Possible uncontrolled tumor related pain present "
                            + "(${Format.concatLowercaseWithCommaAndAnd(activePainMedications)} usage)"
                )
            }

            plannedPainMedications.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Possible uncontrolled tumor related pain present "
                            + "(planned ${Format.concatLowercaseWithCommaAndAnd(plannedPainMedications)} usage)"
                )
            }

            else -> {
                EvaluationFactory.fail("No potential uncontrolled tumor related pain")
            }
        }
    }
}