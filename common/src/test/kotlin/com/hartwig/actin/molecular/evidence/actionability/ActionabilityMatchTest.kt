package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActionabilityMatchTest {

    @Test
    fun `Should extend actionable event with category event false when hotspot, fusion, HLA or characteristic`() {
        assertThat(TestServeActionabilityFactory.withHotspot().molecularCriterium().hotspots().first().isCategoryEvent()).isFalse()
        assertThat(TestServeActionabilityFactory.withFusion().molecularCriterium().fusions().first().isCategoryEvent()).isFalse()
        assertThat(TestServeActionabilityFactory.withHla().molecularCriterium().hla().first().isCategoryEvent()).isFalse()
        assertThat(
            TestServeActionabilityFactory.withCharacteristic().molecularCriterium().characteristics().first().isCategoryEvent()
        ).isFalse()
    }

    @Test
    fun `Should extend actionable event with category event true when range`() {
        assertThat(TestServeActionabilityFactory.withCodon().molecularCriterium().codons().first().isCategoryEvent()).isTrue()
    }

    @Test
    fun `Should extend actionable event with category event false when gene in included list of events, true otherwise`() {
        assertThat(
            TestServeActionabilityFactory.withGene(GeneEvent.AMPLIFICATION).molecularCriterium().genes().first().isCategoryEvent()
        ).isFalse()
        assertThat(
            TestServeActionabilityFactory.withGene(GeneEvent.ANY_MUTATION).molecularCriterium().genes().first().isCategoryEvent()
        ).isTrue()
    }
}