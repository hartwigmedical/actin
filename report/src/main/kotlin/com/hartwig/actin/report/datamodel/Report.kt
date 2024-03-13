package com.hartwig.actin.report.datamodel

import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularHistory

data class Report(
    val patientId: String,
    val clinical: ClinicalRecord,
    val molecularHistory: MolecularHistory,
    val treatmentMatch: TreatmentMatch
)