package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord

class PatientRecordFactoryTest {
    @org.junit.Test
    fun canCreatePatientRecordFromTestRecords() {
        org.junit.Assert.assertNotNull(
            PatientRecordFactory.fromInputs(
                TestClinicalFactory.createMinimalTestClinicalRecord(),
                TestMolecularFactory.createMinimalTestMolecularRecord()
            )
        )
        org.junit.Assert.assertNotNull(
            PatientRecordFactory.fromInputs(
                TestClinicalFactory.createProperTestClinicalRecord(),
                TestMolecularFactory.createProperTestMolecularRecord()
            )
        )
    }

    @org.junit.Test
    fun doNotCrashOnMissingTumorDoids() {
        val base: ClinicalRecord = TestClinicalFactory.createMinimalTestClinicalRecord()
        val noTumorDoid: ClinicalRecord = ImmutableClinicalRecord.builder()
            .from(base)
            .tumor(ImmutableTumorDetails.builder().from(base.tumor()).doids(null).build())
            .build()
        org.junit.Assert.assertNotNull(
            PatientRecordFactory.fromInputs(
                noTumorDoid,
                TestMolecularFactory.createMinimalTestMolecularRecord()
            )
        )
    }

    @org.junit.Test
    fun clinicalPatientBeatsMolecularPatient() {
        val clinical: ClinicalRecord = ImmutableClinicalRecord.builder()
            .from(TestClinicalFactory.createMinimalTestClinicalRecord())
            .patientId("clinical")
            .build()
        val molecular: com.hartwig.actin.molecular.datamodel.MolecularRecord = ImmutableMolecularRecord.builder()
            .from(TestMolecularFactory.createMinimalTestMolecularRecord())
            .patientId("molecular")
            .build()
        val patient = PatientRecordFactory.fromInputs(clinical, molecular)
        assertEquals("clinical", patient.patientId())
    }
}