package com.hartwig.actin.algo.calendar

import com.hartwig.actin.PatientRecord

object ReferenceDateProviderFactory {

    fun create(patientRecord: PatientRecord, runHistorically: Boolean): ReferenceDateProvider {
        return if (runHistorically) HistoricDateProvider.fromPatientDetails(patientRecord.patient) else CurrentDateProvider()
    }
}