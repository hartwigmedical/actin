package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object PatientRecordFactory {
    private val LOGGER: Logger = LogManager.getLogger(PatientRecordFactory::class.java)

    @JvmStatic
    fun fromInputs(clinical: ClinicalRecord, molecular: MolecularRecord?): PatientRecord {
        return if (molecular == null) {
            LOGGER.warn("No molecular data for patient '{}'", clinical.patientId)
            PatientRecord(patientId = clinical.patientId, clinical = clinical, molecular = null)
        } else {
            if (clinical.patientId != molecular.patientId) {
                LOGGER.warn(
                    "Clinical patientId '{}' not the same as molecular patientId '{}'! Using clinical patientId",
                    clinical.patientId,
                    molecular.patientId
                )
            }
            return PatientRecord(patientId = clinical.patientId, clinical = clinical, molecular = molecular)
        }
    }
}
