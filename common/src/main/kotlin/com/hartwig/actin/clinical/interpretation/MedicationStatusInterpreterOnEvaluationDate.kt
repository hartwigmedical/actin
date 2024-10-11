package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.MedicationStatus
import java.time.LocalDate

class MedicationStatusInterpreterOnEvaluationDate(private val evaluationDate: LocalDate, private val todayDate: LocalDate?) :
    MedicationStatusInterpreter {

    override fun interpret(medication: Medication): MedicationStatusInterpretation {
        val startDate = medication.startDate ?: return MedicationStatusInterpretation.UNKNOWN
        val stopDate = medication.stopDate
        when (medication.status) {
            MedicationStatus.CANCELLED -> {
                return MedicationStatusInterpretation.CANCELLED
            }

            MedicationStatus.UNKNOWN -> {
                return MedicationStatusInterpretation.UNKNOWN
            }

            else -> {
                val startIsBeforeEvaluation = startDate.isBefore(evaluationDate)
                val stopIsBeforeEvaluation = stopDate?.isBefore(evaluationDate) ?: false

                return when {
                    startIsBeforeEvaluation && stopIsBeforeEvaluation -> MedicationStatusInterpretation.STOPPED
                    startIsBeforeEvaluation -> {
                        if (todayDate == null && medication.status == MedicationStatus.ON_HOLD) {
                            MedicationStatusInterpretation.STOPPED
                        } else {
                            MedicationStatusInterpretation.ACTIVE
                        }
                    }

                    startDate.isAfter(todayDate ?: evaluationDate) -> {
                        if (medication.status == MedicationStatus.ON_HOLD) {
                            MedicationStatusInterpretation.STOPPED
                        } else {
                            MedicationStatusInterpretation.PLANNED
                        }
                    }

                    else -> MedicationStatusInterpretation.ACTIVE
                }
            }
        }
    }

    companion object {
        private const val MINIMUM_EXPECTED_WEEKS_BEFORE_TRIAL = 2L

        fun createInterpreterForWashout(
            inputWeeks: Int?,
            inputMonths: Int?,
            referenceDate: LocalDate
        ): Pair<MedicationStatusInterpreter, LocalDate> {
            val minDate =
                when {
                    inputWeeks != null && inputMonths == null -> {
                        referenceDate.minusWeeks(inputWeeks.toLong())
                    }

                    inputMonths != null && inputWeeks == null -> {
                        referenceDate.minusMonths(inputMonths.toLong())
                    }

                    else -> {
                        throw IllegalArgumentException("Exactly one of inputWeeks or inputMonths must be provided")
                    }
                }

            return Pair(
                MedicationStatusInterpreterOnEvaluationDate(minDate.plusWeeks(MINIMUM_EXPECTED_WEEKS_BEFORE_TRIAL), referenceDate),
                minDate
            )
        }
    }
}