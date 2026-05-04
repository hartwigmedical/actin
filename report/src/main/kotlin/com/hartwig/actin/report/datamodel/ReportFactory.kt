package com.hartwig.actin.report.datamodel

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate

object ReportFactory {

    private val logger = KotlinLogging.logger {}

    fun create(reportDate: LocalDate, patientRecord: PatientRecord, treatmentMatch: TreatmentMatch): Report {
        if (patientRecord.patientId != treatmentMatch.patientId) {
            logger.warn { "Patient record patientId '${patientRecord.patientId}' not the same as treatment match patientId '${treatmentMatch.patientId}'! Using patient record patientId" }
        }

        return Report(
            reportDate = reportDate,
            patientId = patientRecord.patientId,
            patientRecord = patientRecord,
            treatmentMatch = treatmentMatch
        )
    }
}