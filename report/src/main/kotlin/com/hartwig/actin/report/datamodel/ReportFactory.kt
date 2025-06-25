package com.hartwig.actin.report.datamodel

import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import org.apache.logging.log4j.LogManager
import java.time.LocalDate

object ReportFactory {

    private val LOGGER = LogManager.getLogger(ReportFactory::class.java)

    fun create(reportDate: LocalDate, patient: PatientRecord, treatmentMatch: TreatmentMatch, config: EnvironmentConfiguration): Report {
        if (patient.patientId != treatmentMatch.patientId) {
            LOGGER.warn(
                "Patient record patientId '${patient.patientId}' not the same as " +
                        "treatment match patientId '${treatmentMatch.patientId}'! Using patient record patientId"
            )
        }

        return Report(
            reportDate = reportDate,
            patientId = patient.patientId,
            requestingHospital = config.requestingHospital,
            patientRecord = patient,
            treatmentMatch = treatmentMatch,
            config = config.report,
        )
    }
}