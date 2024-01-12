package com.hartwig.actin.algo.medication

import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import java.time.LocalDate

class MedicationStatusInterpreterOnEvaluationDate(private val evaluationDate: LocalDate) : MedicationStatusInterpreter {

    override fun interpret(medication: Medication): MedicationStatusInterpretation {
        val status = medication.status
        if (status == null) {
            return MedicationStatusInterpretation.UNKNOWN
        } else if (status == MedicationStatus.CANCELLED) {
            return MedicationStatusInterpretation.CANCELLED
        }
        val startDate = medication.startDate ?: return MedicationStatusInterpretation.UNKNOWN
        val startIsBeforeEvaluation = startDate.isBefore(evaluationDate)
        return if (!startIsBeforeEvaluation) {
            MedicationStatusInterpretation.PLANNED
        } else {
            val stopDate = medication.stopDate
            if (stopDate == null) {
                when (status) {
                    MedicationStatus.ON_HOLD -> {
                        MedicationStatusInterpretation.STOPPED
                    }

                    MedicationStatus.ACTIVE -> {
                        MedicationStatusInterpretation.ACTIVE
                    }

                    else -> {
                        MedicationStatusInterpretation.UNKNOWN
                    }
                }
            } else {
                val stopIsBeforeEvaluation = stopDate.isBefore(evaluationDate)
                if (stopIsBeforeEvaluation) MedicationStatusInterpretation.STOPPED else MedicationStatusInterpretation.ACTIVE
            }
        }
    }
}