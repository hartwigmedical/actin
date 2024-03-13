package com.hartwig.actin.report.datamodel

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory

object TestReportFactory {
    fun createMinimalTestReport(): Report {
        return Report(
            patientId = TestDataFactory.TEST_PATIENT,
            clinical = TestClinicalFactory.createMinimalTestClinicalRecord(),
            molecularHistory = TestMolecularFactory.createMinimalTestMolecularHistory(),
            treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch()
        )
    }

    fun createProperTestReport(): Report {
        return createMinimalTestReport().copy(
            clinical = TestClinicalFactory.createProperTestClinicalRecord(),
            molecularHistory = TestMolecularFactory.createProperTestMolecularHistory(),
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        )
    }

    fun createExhaustiveTestReport(): Report {
        return createMinimalTestReport().copy(
            clinical = TestClinicalFactory.createExhaustiveTestClinicalRecord(),
            molecularHistory = TestMolecularFactory.createExhaustiveTestMolecularHistory(),
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        )
    }
}