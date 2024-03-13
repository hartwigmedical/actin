package com.hartwig.actin.report.datamodel

import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import org.apache.logging.log4j.LogManager

object ReportFactory {
    private val LOGGER = LogManager.getLogger(ReportFactory::class.java)

    fun fromInputs(clinical: ClinicalRecord, molecularHistory: MolecularHistory, treatmentMatch: TreatmentMatch): Report {
        if (clinical.patientId != treatmentMatch.patientId) {
            LOGGER.warn(
                "Clinical patientId '{}' not the same as treatment match patientId '{}'! Using clinical patientId",
                clinical.patientId,
                treatmentMatch.patientId
            )
        }
        return Report(
            patientId = clinical.patientId,
            clinical = clinical,
            molecularHistory = molecularHistory,
            treatmentMatch = treatmentMatch
        )
    }
}