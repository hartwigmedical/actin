package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object PatientRecordFactory {

    private val LOGGER: Logger = LogManager.getLogger(PatientRecordFactory::class.java)

    @JvmStatic
    fun fromInputs(clinical: ClinicalRecord, molecularHistory: MolecularHistory?): PatientRecord {
        if (molecularHistory == null || molecularHistory.molecularTests.isEmpty()) {
            LOGGER.warn("No molecular data for patient '{}'", clinical.patientId)
        }
        return PatientRecord(
            patientId = clinical.patientId,
            patient = clinical.patient,
            tumor = clinical.tumor,
            clinicalStatus = clinical.clinicalStatus,
            oncologicalHistory = clinical.oncologicalHistory,
            priorSecondPrimaries = clinical.priorSecondPrimaries,
            priorOtherConditions = clinical.priorOtherConditions,
            complications = clinical.complications,
            labValues = clinical.labValues,
            toxicities = clinical.toxicities,
            intolerances = clinical.intolerances,
            surgeries = clinical.surgeries,
            bodyWeights = clinical.bodyWeights,
            bodyHeights = clinical.bodyHeights,
            vitalFunctions = clinical.vitalFunctions,
            bloodTransfusions = clinical.bloodTransfusions,
            medications = clinical.medications,
            priorIHCTests = clinical.priorIHCTests,
            molecularHistory = molecularHistory ?: MolecularHistory.empty()
        )
    }
}
