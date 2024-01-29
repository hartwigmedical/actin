package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import java.time.LocalDate

// TODO (CB): move this class and the copy in algo and the test files to common module once common is converted to kotlin
class MedicationStatusInterpreterOnEvaluationDate(private val evaluationDate: LocalDate) : MedicationStatusInterpreter {

    override fun interpret(medication: Medication): MedicationStatusInterpretation {
        val startDate = medication.startDate ?: return MedicationStatusInterpretation.UNKNOWN
        when (medication.status) {
            MedicationStatus.CANCELLED -> {
                return MedicationStatusInterpretation.CANCELLED
            }

            MedicationStatus.ON_HOLD -> {
                return MedicationStatusInterpretation.STOPPED
            }

            MedicationStatus.UNKNOWN -> {
                return MedicationStatusInterpretation.UNKNOWN
            }

            else -> {
                val startIsBeforeEvaluation = startDate.isBefore(evaluationDate)
                return if (!startIsBeforeEvaluation) {
                    MedicationStatusInterpretation.PLANNED
                } else {
                    val stopDate = medication.stopDate
                    if (stopDate == null) {
                        MedicationStatusInterpretation.ACTIVE
                    } else {
                        val stopIsBeforeEvaluation = stopDate.isBefore(evaluationDate)
                        if (stopIsBeforeEvaluation) MedicationStatusInterpretation.STOPPED else MedicationStatusInterpretation.ACTIVE
                    }
                }
            }
        }
    }
}