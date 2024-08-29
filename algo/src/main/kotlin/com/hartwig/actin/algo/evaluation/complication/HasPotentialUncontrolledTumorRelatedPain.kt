package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasPotentialUncontrolledTumorRelatedPain(private val interpreter: MedicationStatusInterpreter) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {

        val painComplications = ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record, listOf(SEVERE_PAIN_COMPLICATION))
        if (painComplications.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Patient has complication related to pain: " + concatLowercaseWithAnd(painComplications) +
                        ", potentially indicating uncontrolled tumor related pain",
                "Present " + concatLowercaseWithAnd(painComplications)
            )
        }
        val activePainMedications = record.medications?.filter {
            it.name.equals(SEVERE_PAIN_MEDICATION, ignoreCase = true) && interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE
        }?.map { it.name } ?: return MEDICATION_NOT_PROVIDED

        return if (activePainMedications.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient receives pain medication: " + concatLowercaseWithAnd(activePainMedications) +
                        ", potentially indicating uncontrolled tumor related pain",
                "Receives " + concatLowercaseWithAnd(activePainMedications) + " indicating pain "
            )
        } else
            EvaluationFactory.fail(
                "Patient does not have uncontrolled tumor related pain",
                "No potential uncontrolled tumor related pain"
            )
    }

    companion object {
        const val SEVERE_PAIN_COMPLICATION = "pain"
        const val SEVERE_PAIN_MEDICATION = "hydromorphone"
    }
}