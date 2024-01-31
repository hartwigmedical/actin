package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.PatientDetails
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory

internal object GeneralTestFactory {

    fun withBirthYear(birthYear: Int): PatientRecord {
        return withPatientDetails(TestClinicalFactory.createMinimalTestClinicalRecord().patient.copy(birthYear = birthYear))
    }

    fun withGender(gender: Gender): PatientRecord {
        return withPatientDetails(TestClinicalFactory.createMinimalTestClinicalRecord().patient.copy(gender = gender))
    }

    fun withWHO(who: Int?): PatientRecord {
        return withClinicalStatus(TestClinicalFactory.createMinimalTestClinicalRecord().clinicalStatus.copy(who = who))
    }

    fun withWHOAndComplications(who: Int, complicationCategories: Iterable<String>): PatientRecord {
        val clinicalStatus = TestClinicalFactory.createMinimalTestClinicalRecord().clinicalStatus.copy(who = who)
        val complication = Complication(name = "", categories = complicationCategories.toSet(), year = null, month = null)
        val clinical = TestClinicalFactory.createMinimalTestClinicalRecord()
            .copy(clinicalStatus = clinicalStatus, complications = listOf(complication))
        return withClinicalRecord(clinical)
    }

    fun withBodyWeights(bodyWeights: Iterable<BodyWeight>): PatientRecord {
        val clinical = TestClinicalFactory.createMinimalTestClinicalRecord().copy(bodyWeights = bodyWeights.toList())
        return withClinicalRecord(clinical)
    }

    private fun withPatientDetails(patientDetails: PatientDetails): PatientRecord {
        val clinical = TestClinicalFactory.createMinimalTestClinicalRecord().copy(patient = patientDetails)
        return withClinicalRecord(clinical)
    }

    private fun withClinicalStatus(clinicalStatus: ClinicalStatus): PatientRecord {
        val clinical = TestClinicalFactory.createMinimalTestClinicalRecord().copy(clinicalStatus = clinicalStatus)
        return withClinicalRecord(clinical)
    }

    private fun withClinicalRecord(clinical: ClinicalRecord): PatientRecord {
        return TestDataFactory.createMinimalTestPatientRecord().copy(clinical = clinical)
    }
}