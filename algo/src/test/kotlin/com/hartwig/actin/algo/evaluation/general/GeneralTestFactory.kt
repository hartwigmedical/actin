package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
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
        val complication = Complication(name = "", categories = complicationCategories.toSet(), year = null, month = null)
        return TestPatientFactory.createMinimalTestPatientRecord().copy(
            clinicalStatus = ClinicalStatus(who = who),
            complications = listOf(complication),
        )
    }

    private fun withPatientDetails(patientDetails: PatientDetails): PatientRecord {
        return TestPatientFactory.createMinimalTestPatientRecord().copy(patient = patientDetails)
    }

    private fun withClinicalStatus(clinicalStatus: ClinicalStatus): PatientRecord {
        return TestPatientFactory.createMinimalTestPatientRecord().copy(clinicalStatus = clinicalStatus)
    }
}