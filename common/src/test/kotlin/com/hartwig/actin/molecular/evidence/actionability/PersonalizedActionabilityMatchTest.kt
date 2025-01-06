package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PersonalizedActionabilityMatchTest {

    @Test
    fun `Should extend actionable event with category event false when hotspot, fusion, HLA or characteristic`() {
        assertThat(TestServeEvidenceFactory.createEvidenceForHotspot().molecularCriterium().hotspots().first().isCategoryEvent()).isFalse()
        assertThat(TestServeEvidenceFactory.createEvidenceForFusion().molecularCriterium().fusions().first().isCategoryEvent()).isFalse()
        assertThat(TestServeEvidenceFactory.createEvidenceForHla().molecularCriterium().hla().first().isCategoryEvent()).isFalse()
        assertThat(
            TestServeEvidenceFactory.createEvidenceForCharacteristic().molecularCriterium().characteristics().first().isCategoryEvent()
        ).isFalse()
    }

    @Test
    fun `Should extend actionable event with category event true when range`() {
        assertThat(
            TestServeEvidenceFactory.createEvidenceForCodon().molecularCriterium().codons().first().isCategoryEvent()
        ).isTrue()
    }

    @Test
    fun `Should extend actionable event with category event false when gene in included list of events, true otherwise`() {
        assertThat(
            TestServeEvidenceFactory.createEvidenceForGene(geneEvent = GeneEvent.AMPLIFICATION).molecularCriterium().genes().first()
                .isCategoryEvent()
        ).isFalse()
        assertThat(
            TestServeEvidenceFactory.createEvidenceForGene(geneEvent = GeneEvent.ANY_MUTATION).molecularCriterium().genes().first()
                .isCategoryEvent()
        ).isTrue()
    }
}