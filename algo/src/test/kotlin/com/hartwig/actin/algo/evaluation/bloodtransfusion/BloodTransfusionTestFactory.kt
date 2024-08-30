package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.BloodTransfusion
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory

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
        return PatientRecordFactory.fromInputs(clinical, TestMolecularFactory.createMinimalTestMolecularHistory())
    }
}