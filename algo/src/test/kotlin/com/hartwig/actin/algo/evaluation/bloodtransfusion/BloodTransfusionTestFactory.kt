package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory

internal object BloodTransfusionTestFactory {
    fun withBloodTransfusion(transfusion: BloodTransfusion): PatientRecord {
        return withBloodTransfusions(listOf(transfusion))
    }

    fun withBloodTransfusions(transfusions: List<BloodTransfusion>): PatientRecord {
        return withClinicalRecord(
            ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                .bloodTransfusions(transfusions)
                .build()
        )
    }

    fun withMedication(medication: Medication): PatientRecord {
        return withMedications(listOf(medication))
    }

    fun withMedications(medications: List<Medication>): PatientRecord {
        return withClinicalRecord(
            ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                .medications(medications)
                .build()
        )
    }

    private fun withClinicalRecord(clinical: ClinicalRecord): PatientRecord {
        return ImmutablePatientRecord.builder().from(TestDataFactory.createMinimalTestPatientRecord()).clinical(clinical).build()
    }
}