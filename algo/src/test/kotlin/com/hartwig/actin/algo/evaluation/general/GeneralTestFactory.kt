package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.PerformanceStatus
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.clinical.WhoStatus
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import java.time.LocalDate

internal object GeneralTestFactory {

    fun withBirthYear(birthYear: Int): PatientRecord {
        return withPatientDetails(TestClinicalFactory.createMinimalTestClinicalRecord().patient.copy(birthYear = birthYear))
    }

    fun withGender(gender: Gender): PatientRecord {
        return withPatientDetails(TestClinicalFactory.createMinimalTestClinicalRecord().patient.copy(gender = gender))
    }

    fun withWHO(who: Int?, precision: WhoStatusPrecision = WhoStatusPrecision.EXACT): PatientRecord {
        return withPerformanceStatus(
            TestClinicalFactory.createMinimalTestClinicalRecord().performanceStatus.copy(
                whoStatuses = who?.let { listOf(WhoStatus(LocalDate.now(), it, precision)) } ?: emptyList()
            )
        )
    }

    private fun withPatientDetails(patientDetails: PatientDetails): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(patient = patientDetails)
    }

    private fun withPerformanceStatus(performanceStatus: PerformanceStatus): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(performanceStatus = performanceStatus)
    }
}