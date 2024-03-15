package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object PatientRecordFactory {

    private val LOGGER: Logger = LogManager.getLogger(PatientRecordFactory::class.java)

    @JvmStatic
    fun fromInputs(clinical: ClinicalRecord, molecularHistory: MolecularHistory?): PatientRecord {
        if (molecularHistory == null) {
            LOGGER.warn("No molecular data for patient '{}'", clinical.patientId)
        } else if (clinical.patientId != molecularHistory.patientId) {
            LOGGER.warn(
                "Clinical patientId '{}' not the same as molecular patientId '{}'! Using clinical patientId",
                clinical.patientId,
                molecularHistory.patientId
            )
        }

        return PatientRecord(patientId = clinical.patientId, clinical = clinical,
            molecularHistory = molecularHistory ?: MolecularHistory.empty(clinical.patientId)
        )
    }
}
