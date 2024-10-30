package com.hartwig.actin.report.datamodel

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import java.time.LocalDate
import org.apache.logging.log4j.LogManager

object ReportFactory {
    private val LOGGER = LogManager.getLogger(ReportFactory::class.java)

    fun fromInputs(
        patient: PatientRecord, treatmentMatch: TreatmentMatch, config: ReportConfiguration
    ): Report {
        if (patient.patientId != treatmentMatch.patientId) {
            LOGGER.warn(
                "Patient record patientId '{}' not the same as treatment match patientId '{}'! Using Patient record patientId",
                patient.patientId,
                treatmentMatch.patientId
            )
        }
        return Report(
            reportDate = config.reportDate ?: LocalDate.now(),
            patientId = patient.patientId,
            patientRecord = patient,
            treatmentMatch = treatmentMatch,
            config = config
        )
    }
}