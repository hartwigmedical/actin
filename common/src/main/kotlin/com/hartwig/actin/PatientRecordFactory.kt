package com.hartwig.actin

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import io.github.oshai.kotlinlogging.KotlinLogging

object PatientRecordFactory {

    private val logger = KotlinLogging.logger {}

    fun fromInputs(clinical: ClinicalRecord, molecularTests: List<MolecularTest>): PatientRecord {
        if (molecularTests.isEmpty()) {
            logger.warn { "No molecular data for patient '${clinical.patientId}'" }
        }
    
        return PatientRecord(
            patientId = clinical.patientId,
            patient = clinical.patient,
            tumor = clinical.tumor,
            clinicalStatus = clinical.clinicalStatus,
            performanceStatus = clinical.performanceStatus,
            oncologicalHistory = clinical.oncologicalHistory,
            priorPrimaries = clinical.priorPrimaries,
            comorbidities = clinical.comorbidities,
            ihcTests = clinical.ihcTests,
            labValues = clinical.labValues,
            surgeries = clinical.surgeries,
            bodyWeights = clinical.bodyWeights,
            bodyHeights = clinical.bodyHeights,
            vitalFunctions = clinical.vitalFunctions,
            bloodTransfusions = clinical.bloodTransfusions,
            medications = clinical.medications,
            pathologyReports = clinical.pathologyReports,
            molecularTests = molecularTests
        )
    }
}
