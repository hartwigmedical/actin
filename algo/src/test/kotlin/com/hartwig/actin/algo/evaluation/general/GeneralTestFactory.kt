package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory

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

    private fun withPatientDetails(patientDetails: PatientDetails): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(patient = patientDetails)
    }

    private fun withClinicalStatus(clinicalStatus: ClinicalStatus): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(clinicalStatus = clinicalStatus)
    }
}