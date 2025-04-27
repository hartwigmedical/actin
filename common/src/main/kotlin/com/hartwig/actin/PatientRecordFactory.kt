package com.hartwig.actin

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object PatientRecordFactory {

    private val LOGGER: Logger = LogManager.getLogger(PatientRecordFactory::class.java)

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
            comorbidities = clinical.comorbidities,
            priorIHCTests = clinical.priorIHCTests,
            labValues = clinical.labValues,
            surgeries = clinical.surgeries,
            bodyWeights = clinical.bodyWeights,
            bodyHeights = clinical.bodyHeights,
            vitalFunctions = clinical.vitalFunctions,
            bloodTransfusions = clinical.bloodTransfusions,
            medications = clinical.medications,
            pathologyReports = clinical.pathologyReports,
            molecularHistory = molecularHistory ?: MolecularHistory.empty()
        )
    }
}
