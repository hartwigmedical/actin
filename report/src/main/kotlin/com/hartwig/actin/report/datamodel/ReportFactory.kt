package com.hartwig.actin.report.datamodel

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import org.apache.logging.log4j.LogManager
import java.time.LocalDate

object ReportFactory {

    private val LOGGER = LogManager.getLogger(ReportFactory::class.java)

    fun create(
        reportDate: LocalDate,
        patientRecord: PatientRecord,
        treatmentMatch: TreatmentMatch,
        reportConfiguration: ReportConfiguration
    ): Report {
        if (patientRecord.patientId != treatmentMatch.patientId) {
            LOGGER.warn(
                "Patient record patientId '${patientRecord.patientId}' not the same as " +
                        "treatment match patientId '${treatmentMatch.patientId}'! Using patient record patientId"
            )
        }

        return Report(
            reportDate = reportDate,
            patientId = patientRecord.patientId,
            patientRecord = patientRecord,
            treatmentMatch = treatmentMatch,
            reportConfiguration = reportConfiguration
        )
    }
}