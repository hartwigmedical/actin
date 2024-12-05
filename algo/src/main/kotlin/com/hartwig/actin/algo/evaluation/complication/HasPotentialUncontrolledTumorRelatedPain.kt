package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.othercondition.PriorOtherConditionFunctions
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel

class HasPotentialUncontrolledTumorRelatedPain(private val interpreter: MedicationStatusInterpreter, private val icdModel: IcdModel) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (hasCancerRelatedPainComplicationOrHistory, hasAcutePainComplicationOrHistory) =
            listOf(IcdConstants.CHRONIC_CANCER_RELATED_PAIN_ICD, IcdConstants.ACUTE_PAIN_ICD).map {
                ComplicationFunctions.findComplicationsMatchingAnyIcdCode(record, listOf(it), icdModel).isNotEmpty() ||
                        PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(record, listOf(it), icdModel).isNotEmpty()
            }

        val activePainMedications = record.medications?.filter {
            it.name.equals(SEVERE_PAIN_MEDICATION, ignoreCase = true) && interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE
        }?.map { it.name } ?: return MEDICATION_NOT_PROVIDED

        return when {
            hasCancerRelatedPainComplicationOrHistory -> {
                EvaluationFactory.undetermined("Has tumor related pain in history - undetermined if uncontrolled")
            }

            hasAcutePainComplicationOrHistory -> {
                EvaluationFactory.undetermined("Has acute pain in history - undetermined if uncontrolled")
            }

            activePainMedications.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Receives pain medication - undetermined if uncontrolled tumor related pain present",
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient does not have uncontrolled tumor related pain",
                    "No potential uncontrolled tumor related pain"
                )
            }
        }
    }

    companion object {
        const val SEVERE_PAIN_MEDICATION = "hydromorphone"
    }
}