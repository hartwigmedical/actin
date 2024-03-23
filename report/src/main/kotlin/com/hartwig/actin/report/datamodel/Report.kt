package com.hartwig.actin.report.datamodel

import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.report.EnvironmentConfiguration

data class Report(
    val patientId: String,
    val clinical: ClinicalRecord,
    val molecular: MolecularRecord?,
    val treatmentMatch: TreatmentMatch,
    val config: EnvironmentConfiguration
)