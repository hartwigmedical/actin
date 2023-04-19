package com.hartwig.actin.soc.calendar

import com.hartwig.actin.clinical.datamodel.ClinicalRecord

object ReferenceDateProviderFactory {
    fun create(clinical: ClinicalRecord, runHistorically: Boolean): ReferenceDateProvider {
        return if (runHistorically) HistoricDateProvider.fromClinical(clinical) else CurrentDateProvider()
    }
}