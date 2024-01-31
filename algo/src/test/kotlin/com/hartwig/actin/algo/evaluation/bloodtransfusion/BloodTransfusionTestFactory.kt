package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory

internal object BloodTransfusionTestFactory {
    fun withBloodTransfusion(transfusion: BloodTransfusion): PatientRecord {
        return withBloodTransfusions(listOf(transfusion))
    }

    fun withBloodTransfusions(transfusions: List<BloodTransfusion>): PatientRecord {
        return withClinicalRecord(TestClinicalFactory.createMinimalTestClinicalRecord().copy(bloodTransfusions = transfusions))
    }

    fun withMedication(medication: Medication): PatientRecord {
        return withMedications(listOf(medication))
    }

    fun withMedications(medications: List<Medication>): PatientRecord {
        return withClinicalRecord(TestClinicalFactory.createMinimalTestClinicalRecord().copy(medications = medications))
    }

    private fun withClinicalRecord(clinical: ClinicalRecord): PatientRecord {
        return TestDataFactory.createMinimalTestPatientRecord().copy(clinical = clinical)
    }
}