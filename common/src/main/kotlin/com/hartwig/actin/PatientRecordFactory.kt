package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object PatientRecordFactory {
    private val LOGGER: Logger = LogManager.getLogger(PatientRecordFactory::class.java)

    @JvmStatic
    fun fromInputs(clinical: ClinicalRecord, molecular: MolecularRecord): PatientRecord {
        if (clinical.patientId != molecular.patientId) {
            LOGGER.warn(
                "Clinical patientId '{}' not the same as molecular patientId '{}'! Using clinical patientId",
                clinical.patientId,
                molecular.patientId
            )
        }
        return PatientRecord(
            patientId = clinical.patientId,
            patient = clinical.patient,
            tumor = clinical.tumor,
            clinicalStatus = clinical.clinicalStatus,
            oncologicalHistory = clinical.oncologicalHistory,
            priorSecondPrimaries = clinical.priorSecondPrimaries,
            priorOtherConditions = clinical.priorOtherConditions,
            priorMolecularTests = clinical.priorMolecularTests,
            complications = clinical.complications,
            labValues = clinical.labValues,
            toxicities = clinical.toxicities,
            intolerances = clinical.intolerances,
            surgeries = clinical.surgeries,
            bodyWeights = clinical.bodyWeights,
            vitalFunctions = clinical.vitalFunctions,
            bloodTransfusions = clinical.bloodTransfusions,
            medications = clinical.medications,
            molecular = molecular)
    }
}
