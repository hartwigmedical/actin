package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.molecular.evidence.matching.FUSION_CRITERIA
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FusionLookupTest {

    @Test
    fun `Should lookup fusions`() {
        val fusion1: KnownFusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
        val fusion2: KnownFusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").minExonUp(3).maxExonUp(3).build()
        val fusion3: KnownFusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").minExonDown(4).maxExonDown(4).build()
        val fusion4: KnownFusion = TestServeKnownFactory.fusionBuilder()
            .geneUp("up")
            .geneDown("down")
            .minExonUp(3)
            .maxExonUp(3)
            .minExonDown(4)
            .maxExonDown(4)
            .build()
        val knownFusions = listOf(fusion1, fusion2, fusion3, fusion4)

        val broadMatch = FUSION_CRITERIA.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 2, fusedExonDown = 5)
        assertThat(FusionLookup.find(knownFusions, broadMatch)).isEqualTo(fusion1)

        val specificUpMatch = FUSION_CRITERIA.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 3, fusedExonDown = 5)
        assertThat(FusionLookup.find(knownFusions, specificUpMatch)).isEqualTo(fusion2)

        val specificDownMatch = FUSION_CRITERIA.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 2, fusedExonDown = 4)
        assertThat(FusionLookup.find(knownFusions, specificDownMatch)).isEqualTo(fusion3)

        val specificMatch = FUSION_CRITERIA.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 3, fusedExonDown = 4)
        assertThat(FusionLookup.find(knownFusions, specificMatch)).isEqualTo(fusion4)

        val noMatch = FUSION_CRITERIA.copy(geneStart = "down", geneEnd = "up")
        assertThat(FusionLookup.find(knownFusions, noMatch)).isNull()
    }
}