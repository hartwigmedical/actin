package com.hartwig.actin.report.datamodel

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch

data class Report(
    val patientId: String,
    val patientRecord: PatientRecord,
    val treatmentMatch: TreatmentMatch,
    val config: ReportConfiguration
)