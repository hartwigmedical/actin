package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.*
import org.apache.logging.log4j.util.Strings

internal object GeneralTestFactory {
    fun withBirthYear(birthYear: Int): PatientRecord {
        val patientDetails: PatientDetails = ImmutablePatientDetails.builder()
            .from(TestClinicalFactory.createMinimalTestClinicalRecord().patient())
            .birthYear(birthYear)
            .build()
        return withPatientDetails(patientDetails)
    }

    fun withGender(gender: Gender): PatientRecord {
        val patientDetails: PatientDetails = ImmutablePatientDetails.builder()
            .from(TestClinicalFactory.createMinimalTestClinicalRecord().patient())
            .gender(gender)
            .build()
        return withPatientDetails(patientDetails)
    }

    fun withWHO(who: Int?): PatientRecord {
        val clinicalStatus: ClinicalStatus = ImmutableClinicalStatus.builder()
            .from(TestClinicalFactory.createMinimalTestClinicalRecord().clinicalStatus())
            .who(who)
            .build()
        return withClinicalStatus(clinicalStatus)
    }

    fun withWHOAndComplications(who: Int, complicationCategories: Iterable<String>): PatientRecord {
        val clinicalStatus: ClinicalStatus = ImmutableClinicalStatus.builder()
            .from(TestClinicalFactory.createMinimalTestClinicalRecord().clinicalStatus())
            .who(who)
            .build()
        val complication: Complication = ImmutableComplication.builder().name(Strings.EMPTY).categories(complicationCategories).build()
        val clinical: ClinicalRecord = ImmutableClinicalRecord.builder()
            .from(TestClinicalFactory.createMinimalTestClinicalRecord())
            .clinicalStatus(clinicalStatus)
            .addComplications(complication)
            .build()
        return withClinicalRecord(clinical)
    }

    fun withBodyWeights(bodyWeights: Iterable<BodyWeight?>): PatientRecord {
        val clinical: ClinicalRecord = ImmutableClinicalRecord.builder()
            .from(TestClinicalFactory.createMinimalTestClinicalRecord())
            .bodyWeights(bodyWeights)
            .build()
        return withClinicalRecord(clinical)
    }

    private fun withPatientDetails(patientDetails: PatientDetails): PatientRecord {
        val clinical: ClinicalRecord = ImmutableClinicalRecord.builder()
            .from(TestClinicalFactory.createMinimalTestClinicalRecord())
            .patient(patientDetails)
            .build()
        return withClinicalRecord(clinical)
    }

    private fun withClinicalStatus(clinicalStatus: ClinicalStatus): PatientRecord {
        val clinical: ClinicalRecord = ImmutableClinicalRecord.builder()
            .from(TestClinicalFactory.createMinimalTestClinicalRecord())
            .clinicalStatus(clinicalStatus)
            .build()
        return withClinicalRecord(clinical)
    }

    private fun withClinicalRecord(clinical: ClinicalRecord): PatientRecord {
        return ImmutablePatientRecord.builder().from(TestDataFactory.createMinimalTestPatientRecord()).clinical(clinical).build()
    }
}