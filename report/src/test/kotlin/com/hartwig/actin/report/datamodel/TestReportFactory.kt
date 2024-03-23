package com.hartwig.actin.report.datamodel

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.report.EnvironmentConfiguration

object TestReportFactory {
    fun createMinimalTestReport(): Report {
        return Report(
            patientId = TestPatientFactory.TEST_PATIENT,
            clinical = TestClinicalFactory.createMinimalTestClinicalRecord(),
            molecular = TestMolecularFactory.createMinimalTestMolecularRecord(),
            treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch(),
            config = EnvironmentConfiguration()
        )
    }

    fun createProperTestReport(): Report {
        return createMinimalTestReport().copy(
            clinical = TestClinicalFactory.createProperTestClinicalRecord(),
            molecular = TestMolecularFactory.createProperTestMolecularRecord(),
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        )
    }

    fun createExhaustiveTestReport(): Report {
        return createMinimalTestReport().copy(
            clinical = TestClinicalFactory.createExhaustiveTestClinicalRecord(),
            molecular = TestMolecularFactory.createExhaustiveTestMolecularRecord(),
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        )
    }

    fun createExhaustiveTestReportWithoutMolecular(): Report {
        return createMinimalTestReport().copy(
            clinical = TestClinicalFactory.createExhaustiveTestClinicalRecord(),
            molecular = null,
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        )
    }
}