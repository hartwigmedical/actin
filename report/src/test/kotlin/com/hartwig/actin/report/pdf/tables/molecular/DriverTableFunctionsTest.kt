package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DriverTableFunctionsTest {
    private val trial1 = TestClinicalEvidenceFactory.createExternalTrial("trial 1")
    private val trial2 = TestClinicalEvidenceFactory.createExternalTrial("trial 2")
    private val trial3 = TestClinicalEvidenceFactory.createExternalTrial("trial 3")

    @Test
    fun `Should correctly group by single event`() {
        val externalTrials = mutableMapOf("PTEN del" to setOf(trial1), "PTEN del, MYC amp" to setOf(trial2, trial3))
        val groupedByEvent = DriverTableFunctions.groupByEvent(externalTrials)
        assertThat(groupedByEvent).isEqualTo(mapOf("PTEN del" to setOf(trial1, trial2, trial3), "MYC amp" to setOf(trial2, trial3)))
    }

}