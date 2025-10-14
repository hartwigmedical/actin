package com.hartwig.actin.report.datamodel

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import java.time.LocalDate

data class Report(
    val reportDate: LocalDate,
    val patientId: String,
    val patientRecord: PatientRecord,
    val treatmentMatch: TreatmentMatch,
    val configuration: ReportConfiguration
)