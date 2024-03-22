package com.hartwig.actin.report.datamodel

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.TreatmentMatch

data class Report(
    val patientId: String,
    val patientRecord: PatientRecord,
    val treatmentMatch: TreatmentMatch
)