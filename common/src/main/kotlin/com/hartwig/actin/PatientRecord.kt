package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularHistory

data class PatientRecord(
    val patientId: String,
    val clinical: ClinicalRecord,
    val molecularHistory: MolecularHistory
)
