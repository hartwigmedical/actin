package com.hartwig.actin.molecular.evidence

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ServeLoaderTest {

    private val indication1 = TestServeFactory.createIndicationWithDoid("doid1")
    private val indication2 = TestServeFactory.createIndicationWithDoid("doid2")
    private val molecularCriterium1 = TestServeMolecularFactory.createHotspot()
    private val molecularCriterium2 = TestServeMolecularFactory.createGene()

    @Test
    fun `Can expand trials for molecular criteria and indications`() {
        val actionableTrial =
            TestServeTrialFactory.create(
                molecularCriteria = setOf(molecularCriterium1, molecularCriterium2),
                indications = setOf(indication1, indication2)
            )

        val expandedTrials = ServeLoader.expandTrials(listOf(actionableTrial))

        val expandedTrial1 = TestServeTrialFactory.create(molecularCriteria = setOf(molecularCriterium1), indications = setOf(indication1))
        val expandedTrial2 = TestServeTrialFactory.create(molecularCriteria = setOf(molecularCriterium1), indications = setOf(indication2))
        val expandedTrial3 = TestServeTrialFactory.create(molecularCriteria = setOf(molecularCriterium2), indications = setOf(indication1))
        val expandedTrial4 = TestServeTrialFactory.create(molecularCriteria = setOf(molecularCriterium2), indications = setOf(indication2))

        assertThat(expandedTrials).containsAll(listOf(expandedTrial1, expandedTrial2, expandedTrial3, expandedTrial4))
    }
}