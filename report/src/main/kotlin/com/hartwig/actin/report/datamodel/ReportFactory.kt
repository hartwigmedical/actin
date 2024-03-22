package com.hartwig.actin.report.datamodel

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import org.apache.logging.log4j.LogManager

object ReportFactory {
    private val LOGGER = LogManager.getLogger(ReportFactory::class.java)

    fun fromInputs(patient: PatientRecord, treatmentMatch: TreatmentMatch): Report {
        if (patient.patientId != treatmentMatch.patientId) {
            LOGGER.warn(
                "Patient record patientId '{}' not the same as treatment match patientId '{}'! Using Patient record patientId",
                patient.patientId,
                treatmentMatch.patientId
            )
        }
        return Report(
            patientId = patient.patientId,
            patientRecord = patient,
            treatmentMatch = treatmentMatch
        )
    }
}