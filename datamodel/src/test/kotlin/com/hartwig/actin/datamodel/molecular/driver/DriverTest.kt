package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DriverTest {

    @Test
    fun `Should format variant event correctly`() {
        val variant = TestMolecularFactory.createMinimalVariant().copy(
            event = "BRAF V600E",
            sourceEvent = "BRAF V500E"
        )
        assertThat(variant.eventDisplay()).isEqualTo("BRAF V600E (also known as BRAF V500E)")
    }

    @Test
    fun `Should format copy nr gain correctly when full gain and min copy nr is known`() {
        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = "BRAF",
            event = "BRAF amp",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact()
                .copy(type = CopyNumberType.FULL_GAIN, minCopies = 100, maxCopies = 100)
        )
        assertThat(copyNumber.eventDisplay()).isEqualTo("BRAF 100 copies")
    }

    @Test
    fun `Should format copy nr gain correctly when full gain and min copy nr is not known`() {
        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = "BRAF",
            event = "BRAF amp",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact()
                .copy(type = CopyNumberType.FULL_GAIN)
        )
        assertThat(copyNumber.eventDisplay()).isEqualTo("BRAF")
    }

    @Test
    fun `Should format copy nr gain correctly when partial gain and min and max copy nr is known`() {
        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = "BRAF",
            event = "BRAF amp",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact()
                .copy(type = CopyNumberType.PARTIAL_GAIN, minCopies = 1, maxCopies = 100)
        )
        assertThat(copyNumber.eventDisplay()).isEqualTo("BRAF 100 copies (partial)")
    }

    @Test
    fun `Should format copy nr gain correctly when partial gain and min and max copy nr is not known`() {
        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = "BRAF",
            event = "BRAF amp",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact()
                .copy(type = CopyNumberType.PARTIAL_GAIN)
        )
        assertThat(copyNumber.eventDisplay()).isEqualTo("BRAF (partial)")
    }

    @Test
    fun `Should format copy nr gain correctly when amp but not on canonical transcript`() {
        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = "BRAF",
            event = "BRAF amp",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact().copy(type = CopyNumberType.NONE),
            otherImpacts = setOf(TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact().copy(type = CopyNumberType.FULL_GAIN))
        )
        assertThat(copyNumber.eventDisplay()).isEqualTo("BRAF (alt transcript)")
    }

    @Test
    fun `Should format deletion correctly when on canonical transcript`() {
        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = "BRAF",
            event = "BRAF del",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact().copy(type = CopyNumberType.FULL_DEL),
        )
        assertThat(copyNumber.eventDisplay()).isEqualTo("BRAF")
    }

    @Test
    fun `Should format deletion correctly when on non-canonical transcript`() {
        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = "BRAF",
            event = "BRAF del",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact().copy(type = CopyNumberType.NONE),
            otherImpacts = setOf(TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact().copy(type = CopyNumberType.FULL_DEL))
        )
        assertThat(copyNumber.eventDisplay()).isEqualTo("BRAF (alt transcript)")
    }

    @Test
    fun `Should format copy nr correctly`() {
        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = "BRAF",
            event = "BRAF copy nr",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact().copy(type = CopyNumberType.NONE),
            otherImpacts = setOf(TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact().copy(type = CopyNumberType.NONE))
        )
        assertThat(copyNumber.eventDisplay()).isEqualTo("BRAF copy nr")
    }

    @Test
    fun `Should format virus correctly when there are integrations`() {
        val virus = TestMolecularFactory.createMinimalVirus().copy(
            event = "HPV positive",
            integrations = 5
        )
        assertThat(virus.eventDisplay()).isEqualTo("HPV positive (5 integrations detected)")
    }

    @Test
    fun `Should not alter virus events when there are no integrations`() {
        val virus = TestMolecularFactory.createMinimalVirus().copy(
            event = "HPV positive",
        )
        assertThat(virus.eventDisplay()).isEqualTo("HPV positive")
    }

    @Test
    fun `Should not alter fusion events`() {
        val fusion = TestMolecularFactory.createMinimalFusion().copy(event = "Fusion event")
        assertThat(fusion.eventDisplay()).isEqualTo("Fusion event")
    }

    @Test
    fun `Should not alter disruption events`() {
        val disruption = TestMolecularFactory.createMinimalDisruption().copy(event = "Disruption event")
        assertThat(disruption.eventDisplay()).isEqualTo("Disruption event")
    }

    @Test
    fun `Should not alter hom disruption events`() {
        val homDisruption = TestMolecularFactory.createMinimalHomozygousDisruption().copy(event = "Hom disruption event")
        assertThat(homDisruption.eventDisplay()).isEqualTo("Hom disruption event")
    }
}