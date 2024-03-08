package com.hartwig.actin.algo.calendar

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import org.checkerframework.checker.units.qual.Current

object ReferenceDateProviderFactory {
    fun create(clinical: ClinicalRecord, runHistorically: Boolean): ReferenceDateProvider {
        return if (runHistorically) HistoricDateProvider.fromClinical(clinical) else CurrentDateProvider()
    }

    fun create(patientRecord: PatientRecord, runHistorically: Boolean): ReferenceDateProvider {
        return if (runHistorically) HistoricDateProvider.fromPatientRecord(patientRecord) else CurrentDateProvider()
    }
}