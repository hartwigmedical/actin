package com.hartwig.actin.algo.calendar

import com.hartwig.actin.datamodel.clinical.PatientDetails
import java.time.LocalDate

internal class HistoricDateProvider private constructor(private val historicDate: LocalDate) : ReferenceDateProvider {

    override fun date(): LocalDate {
        return historicDate
    }

    override val isLive: Boolean
        get() = false

    companion object {
        fun fromPatientDetails(patientDetails: PatientDetails): HistoricDateProvider {
            val historicDate = patientDetails.registrationDate.plusWeeks(3)
            val currentDate = LocalDate.now()
            val effectiveDate = if (currentDate.isBefore(historicDate)) currentDate else historicDate
            return HistoricDateProvider(effectiveDate)
        }
    }
}