package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularClinicalEvidenceFunctionsTest {

    @Test
    fun `Should return driver events and associated evidence`() {
        val events =
            MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(TestMolecularFactory.createExhaustiveTestMolecularHistory())
        assertThat(events).containsExactly("test" to TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence())
    }

}