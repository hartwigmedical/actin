package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.BodyWeight
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
        val clinicalStatus = TestDataFactory.createMinimalTestPatientRecord().clinicalStatus.copy(who = who)
        val complication = Complication(name = "", categories = complicationCategories.toSet(), year = null, month = null)
        val patientRecord = TestDataFactory.createMinimalTestPatientRecord().copy(
            clinicalStatus = clinicalStatus.copy(who = who),
            complications = listOf(complication),
        )
        return patientRecord
    }

    fun withBodyWeights(bodyWeights: Iterable<BodyWeight>): PatientRecord {
        val patientRecord = TestDataFactory.createMinimalTestPatientRecord().copy(bodyWeights = bodyWeights.toList())
        return patientRecord
    }

    private fun withPatientDetails(patientDetails: PatientDetails): PatientRecord {
        val patientRecord = TestDataFactory.createMinimalTestPatientRecord().copy(patient = patientDetails)
        return patientRecord
    }

    private fun withClinicalStatus(clinicalStatus: ClinicalStatus): PatientRecord {
        val patientRecord = TestDataFactory.createMinimalTestPatientRecord().copy(clinicalStatus = clinicalStatus)
        return patientRecord
    }
}