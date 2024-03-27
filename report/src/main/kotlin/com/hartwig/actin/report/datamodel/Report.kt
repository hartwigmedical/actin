package com.hartwig.actin.report.datamodel

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.report.ReportConfiguration

data class Report(
    val patientId: String,
    val patientRecord: PatientRecord,
    val treatmentMatch: TreatmentMatch,
    val config: ReportConfiguration
)