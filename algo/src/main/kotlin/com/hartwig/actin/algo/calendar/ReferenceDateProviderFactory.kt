package com.hartwig.actin.algo.calendar

import com.hartwig.actin.clinical.datamodel.ClinicalRecord

object ReferenceDateProviderFactory {
    fun create(clinical: ClinicalRecord, runHistorically: Boolean): ReferenceDateProvider {
        return if (runHistorically) HistoricDateProvider.fromClinical(clinical) else CurrentDateProvider()
    }
}