package com.hartwig.actin.report.datamodel

import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord

data class Report(
    val patientId: String,
    val clinical: ClinicalRecord,
    val molecular: MolecularRecord?,
    val treatmentMatch: TreatmentMatch,
    val molecularHistory: MolecularHistory = MolecularHistory(emptyList(), "N/A"),
)