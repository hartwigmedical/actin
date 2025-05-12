package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val FUSION = TestMolecularFactory.createMinimalFusion().copy(
    isReportable = true,
    geneStart = "up",
    geneEnd = "down",
    driverType = FusionDriverType.KNOWN_PAIR,
)

private val GENERIC_FUSION = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
private val EXON_AWARE_FUSION =
    TestServeKnownFactory.fusionBuilder().from(GENERIC_FUSION).minExonUp(3).maxExonUp(4).minExonDown(6).maxExonDown(7).build()

class FusionMatchingTest {

    @Test
    fun `Should return false on non-matching gene`() {
        val noMatch = FUSION.copy(geneStart = "down", geneEnd = "up")
        assertThat(FusionMatching.isGeneMatch(GENERIC_FUSION, noMatch)).isFalse()
        assertThat(FusionMatching.isGeneMatch(EXON_AWARE_FUSION, noMatch)).isFalse()
    }

    @Test
    fun `Should match on generic fusion`() {
        assertThat(FusionMatching.isGeneMatch(GENERIC_FUSION, FUSION)).isTrue()
        assertThat(FusionMatching.isExonMatch(GENERIC_FUSION, FUSION)).isTrue()
        assertThat(FusionMatching.explicitlyMatchesExonUp(GENERIC_FUSION, FUSION)).isFalse()
        assertThat(FusionMatching.explicitlyMatchesExonDown(GENERIC_FUSION, FUSION)).isFalse()
        assertThat(FusionMatching.isGeneMatch(EXON_AWARE_FUSION, FUSION)).isTrue()
        assertThat(FusionMatching.isExonMatch(EXON_AWARE_FUSION, FUSION)).isFalse()
        assertThat(FusionMatching.explicitlyMatchesExonUp(EXON_AWARE_FUSION, FUSION)).isFalse()
        assertThat(FusionMatching.explicitlyMatchesExonDown(EXON_AWARE_FUSION, FUSION)).isFalse()
    }

    @Test
    fun `Should match on exact match to exon aware fusion`() {
        val exactMatch = FUSION.copy(fusedExonUp = 4, fusedExonDown = 6)

        assertThat(FusionMatching.isGeneMatch(GENERIC_FUSION, exactMatch)).isTrue()
        assertThat(FusionMatching.isExonMatch(GENERIC_FUSION, exactMatch)).isTrue()
        assertThat(FusionMatching.explicitlyMatchesExonUp(GENERIC_FUSION, exactMatch)).isFalse()
        assertThat(FusionMatching.explicitlyMatchesExonDown(GENERIC_FUSION, exactMatch)).isFalse()
        assertThat(FusionMatching.isGeneMatch(EXON_AWARE_FUSION, exactMatch)).isTrue()
        assertThat(FusionMatching.isExonMatch(EXON_AWARE_FUSION, exactMatch)).isTrue()
        assertThat(FusionMatching.explicitlyMatchesExonUp(EXON_AWARE_FUSION, exactMatch)).isTrue()
        assertThat(FusionMatching.explicitlyMatchesExonDown(EXON_AWARE_FUSION, exactMatch)).isTrue()
    }

    @Test
    fun `Should match on exon up match to exon aware fusion`() {
        val exonUpMatch = FUSION.copy(fusedExonUp = 3, fusedExonDown = 8)

        assertThat(FusionMatching.isGeneMatch(GENERIC_FUSION, exonUpMatch)).isTrue()
        assertThat(FusionMatching.isExonMatch(GENERIC_FUSION, exonUpMatch)).isTrue()
        assertThat(FusionMatching.explicitlyMatchesExonUp(GENERIC_FUSION, exonUpMatch)).isFalse()
        assertThat(FusionMatching.explicitlyMatchesExonDown(GENERIC_FUSION, exonUpMatch)).isFalse()
        assertThat(FusionMatching.isGeneMatch(EXON_AWARE_FUSION, exonUpMatch)).isTrue()
        assertThat(FusionMatching.isExonMatch(EXON_AWARE_FUSION, exonUpMatch)).isFalse()
        assertThat(FusionMatching.explicitlyMatchesExonUp(EXON_AWARE_FUSION, exonUpMatch)).isTrue()
        assertThat(FusionMatching.explicitlyMatchesExonDown(EXON_AWARE_FUSION, exonUpMatch)).isFalse()
    }

    @Test
    fun `Should match on exon down match to exon aware fusion`() {
        val exonDownMatch = FUSION.copy(fusedExonUp = 2, fusedExonDown = 7)

        assertThat(FusionMatching.isGeneMatch(GENERIC_FUSION, exonDownMatch)).isTrue()
        assertThat(FusionMatching.isExonMatch(GENERIC_FUSION, exonDownMatch)).isTrue()
        assertThat(FusionMatching.explicitlyMatchesExonUp(GENERIC_FUSION, exonDownMatch)).isFalse()
        assertThat(FusionMatching.explicitlyMatchesExonDown(GENERIC_FUSION, exonDownMatch)).isFalse()
        assertThat(FusionMatching.isGeneMatch(EXON_AWARE_FUSION, exonDownMatch)).isTrue()
        assertThat(FusionMatching.isExonMatch(EXON_AWARE_FUSION, exonDownMatch)).isFalse()
        assertThat(FusionMatching.explicitlyMatchesExonUp(EXON_AWARE_FUSION, exonDownMatch)).isFalse()
        assertThat(FusionMatching.explicitlyMatchesExonDown(EXON_AWARE_FUSION, exonDownMatch)).isTrue()
    }
}