package com.hartwig.actin.report.datamodel

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory

object TestReportFactory {
    fun createMinimalTestReport(): Report {
        return ImmutableReport.builder()
            .patientId(TestDataFactory.TEST_PATIENT)
            .clinical(TestClinicalFactory.createMinimalTestClinicalRecord())
            .molecular(TestMolecularFactory.createMinimalTestMolecularRecord())
            .treatmentMatch(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
            .build()
    }

    fun createProperTestReport(): Report {
        return ImmutableReport.builder()
            .from(createMinimalTestReport())
            .clinical(TestClinicalFactory.createProperTestClinicalRecord())
            .molecular(TestMolecularFactory.createProperTestMolecularRecord())
            .treatmentMatch(TestTreatmentMatchFactory.createProperTreatmentMatch())
            .build()
    }

    fun createExhaustiveTestReport(): Report {
        return ImmutableReport.builder()
            .from(createMinimalTestReport())
            .clinical(TestClinicalFactory.createProperTestClinicalRecord())
            .molecular(TestMolecularFactory.createExhaustiveTestMolecularRecord())
            .treatmentMatch(TestTreatmentMatchFactory.createProperTreatmentMatch())
            .build()
    }
}