package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val FUSION = TestMolecularFactory.createMinimalFusion().copy(
    isReportable = true,
    geneStart = "up",
    geneEnd = "down",
    driverType = FusionDriverType.KNOWN_PAIR,
)

class FusionLookupTest {

    @Test
    fun `Should lookup fusions`() {
        val fusion1 = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
        val fusion2 = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").minExonUp(3).maxExonUp(3).build()
        val fusion3 = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").minExonDown(4).maxExonDown(4).build()
        val fusion4 = TestServeKnownFactory.fusionBuilder()
            .geneUp("up")
            .geneDown("down")
            .minExonUp(3)
            .maxExonUp(3)
            .minExonDown(4)
            .maxExonDown(4)
            .build()
        val fusion5 = TestServeKnownFactory.fusionBuilder().geneUp("down").geneDown("up").proteinEffect(ProteinEffect.UNKNOWN).build()
        val knownFusions = listOf(fusion1, fusion2, fusion3, fusion4)

        val broadMatch = FUSION.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 2, fusedExonDown = 5)
        assertThat(FusionLookup.find(knownFusions, broadMatch)).isEqualTo(fusion1)

        val specificUpMatch = FUSION.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 3, fusedExonDown = 5)
        assertThat(FusionLookup.find(knownFusions, specificUpMatch)).isEqualTo(fusion2)

        val specificDownMatch = FUSION.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 2, fusedExonDown = 4)
        assertThat(FusionLookup.find(knownFusions, specificDownMatch)).isEqualTo(fusion3)

        val specificMatch = FUSION.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 3, fusedExonDown = 4)
        assertThat(FusionLookup.find(knownFusions, specificMatch)).isEqualTo(fusion4)

        val noMatch = FUSION.copy(geneStart = "down", geneEnd = "up")
        assertThat(FusionLookup.find(knownFusions, noMatch)).isEqualTo(fusion5)
    }
}