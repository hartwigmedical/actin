package com.hartwig.actin.report.datamodel

import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import org.apache.logging.log4j.LogManager
import java.time.LocalDate

object ReportFactory {

    private val LOGGER = LogManager.getLogger(ReportFactory::class.java)

    fun fromInputs(
        patient: PatientRecord, treatmentMatch: TreatmentMatch, config: EnvironmentConfiguration
    ): Report {
        if (patient.patientId != treatmentMatch.patientId) {
            LOGGER.warn(
                "Patient record patientId '{}' not the same as treatment match patientId '{}'! Using patient record patientId",
                patient.patientId,
                treatmentMatch.patientId
            )
        }

        return Report(
            reportDate = config.report.reportDate ?: LocalDate.now(),
            patientId = patient.patientId,
            requestingHospital = config.requestingHospital,
            patientRecord = patient,
            treatmentMatch = treatmentMatch,
            config = config.report,
        )
    }
}