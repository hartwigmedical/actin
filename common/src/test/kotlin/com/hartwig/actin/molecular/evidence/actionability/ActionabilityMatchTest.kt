package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActionabilityMatchTest {

    @Test
    fun `Should extend actionable event with category event false when hotspot, fusion, HLA or characteristic`() {
        assertThat(TestServeActionabilityFactory.hotspotBuilder().build().isCategoryEvent()).isFalse()
        assertThat(TestServeActionabilityFactory.fusionBuilder().build().isCategoryEvent()).isFalse()
        assertThat(TestServeActionabilityFactory.hlaBuilder().build().isCategoryEvent()).isFalse()
        assertThat(TestServeActionabilityFactory.characteristicBuilder().build().isCategoryEvent()).isFalse()
    }

    @Test
    fun `Should extend actionable event with category event true when range`() {
        assertThat(TestServeActionabilityFactory.rangeBuilder().build().isCategoryEvent()).isTrue()
    }

    @Test
    fun `Should extend actionable event with category event false when gene in included list of events, true otherwise`() {
        assertThat(TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).build().isCategoryEvent()).isFalse()
        assertThat(TestServeActionabilityFactory.geneBuilder().event(GeneEvent.ANY_MUTATION).build().isCategoryEvent()).isTrue()
    }
}