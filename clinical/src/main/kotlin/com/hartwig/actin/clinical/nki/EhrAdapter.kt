package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.datamodel.ClinicalRecord

class EhrAdapter<T> {

    fun extract(dataFeed: EhrDataFeed): List<ClinicalRecord>{

        for (ehrPatientRecord in dataFeed.ingest()) {

        }

        return emptyList()
    }
}