package com.hartwig.actin.algo.calendar

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import java.time.LocalDate

internal class HistoricDateProvider private constructor(private val historicDate: LocalDate) : ReferenceDateProvider {
    override fun date(): LocalDate {
        return historicDate
    }

    override val isLive: Boolean
        get() = false

    companion object {
        fun fromClinical(clinical: ClinicalRecord): HistoricDateProvider {
            val historicDate = clinical.patient.registrationDate.plusWeeks(3)
            val currentDate = LocalDate.now()
            val effectiveDate = if (currentDate.isBefore(historicDate)) currentDate else historicDate
            return HistoricDateProvider(effectiveDate)
        }

        fun fromPatientRecord(patientRecord: PatientRecord): HistoricDateProvider {
            val historicDate = patientRecord.patient.registrationDate.plusWeeks(3)
            val currentDate = LocalDate.now()
            val effectiveDate = if (currentDate.isBefore(historicDate)) currentDate else historicDate
            return HistoricDateProvider(effectiveDate)
        }
    }
}