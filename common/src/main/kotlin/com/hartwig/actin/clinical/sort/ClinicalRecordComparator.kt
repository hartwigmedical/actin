package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.ClinicalRecord

class ClinicalRecordComparator : Comparator<ClinicalRecord> {

    override fun compare(record1: ClinicalRecord, record2: ClinicalRecord): Int {
        return record1.patientId.compareTo(record2.patientId)
    }
}
