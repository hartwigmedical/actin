package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.actin.molecular.evidence.actionability.TestIndirectEvidenceFactory.createHotspotEvidence
import com.hartwig.actin.molecular.evidence.actionability.TestIndirectEvidenceFactory.createKnownHotspot
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import com.hartwig.serve.datamodel.molecular.hotspot.VariantAnnotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IndirectEvidenceMatcherTest {

    @Test
    fun `create should include only non resistant related evidences`() {
        val resistantVariant = variantAnnotation(
            gene = "BRAF",
            chromosome = "7",
            position = 140453136,
            ref = "T",
            alt = "A"
        )
        val nonResistantVariant = variantAnnotation(
            gene = "KRAS",
            chromosome = "12",
            position = 25245350,
            ref = "C",
            alt = "T"
        )

        val resistantHotspot = TestServeMolecularFactory.hotspot(resistantVariant)
        val nonResistantHotspot = TestServeMolecularFactory.hotspot(nonResistantVariant)

        val resistantEvidence = TestServeEvidenceFactory.create(
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
                variants = setOf(resistantVariant)
            ),
            treatment = "Resistant Treatment"
        )
        val nonResistantEvidence = createHotspotEvidence(
            hotspot = nonResistantHotspot,
            treatmentName = "Non Resistant Treatment",
            drugClass = "KRAS Inhibitor"
        ).evidence

        val resistantKnownHotspot = createKnownHotspot(
            variant = resistantHotspot.firstVariant(),
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            associatedWithDrugResistance = true
        )

        val nonResistantKnownHotspot = createKnownHotspot(
            variant = nonResistantHotspot.firstVariant(),
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION
        )

        val actinProteinEffect = GeneAlterationFactory.convertProteinEffect(ProteinEffect.GAIN_OF_FUNCTION)

        val matcher = IndirectEvidenceMatcher.create(
            listOf(resistantEvidence, nonResistantEvidence),
            setOf(resistantKnownHotspot, nonResistantKnownHotspot)
        )

        val resistantVariantDriver = TestVariantFactory.createMinimal().copy(
            gene = "BRAF",
            proteinEffect = actinProteinEffect
        )
        val nonResistantVariantDriver = TestVariantFactory.createMinimal().copy(
            gene = "KRAS",
            proteinEffect = actinProteinEffect
        )

        assertThat(matcher.findIndirectEvidence(resistantVariantDriver)).isEmpty()
        assertThat(matcher.findIndirectEvidence(nonResistantVariantDriver))
            .containsExactly(nonResistantEvidence)
    }

    @Test
    fun `Should exclude hotspots without gain or loss of function`() {
        val unknownEffectVariant = variantAnnotation(
            gene = "PIK3CA",
            chromosome = "3",
            position = 178936091,
            ref = "A",
            alt = "G"
        )

        val unknownEffectHotspot = TestServeMolecularFactory.hotspot(unknownEffectVariant)

        val unknownEffectEvidence = createHotspotEvidence(
            hotspot = unknownEffectHotspot,
            treatmentName = "Generic Inhibitor Treatment",
            drugClass = "PIK3CA Inhibitor"
        ).evidence

        val unknownEffectKnownHotspot = createKnownHotspot(
            variant = unknownEffectHotspot.firstVariant(),
            proteinEffect = ProteinEffect.UNKNOWN
        )

        val matcher = IndirectEvidenceMatcher.create(listOf(unknownEffectEvidence), setOf(unknownEffectKnownHotspot))

        val unknownEffectVariantDriver = TestVariantFactory.createMinimal().copy(
            gene = unknownEffectVariant.gene(),
            proteinEffect = GeneAlterationFactory.convertProteinEffect(ProteinEffect.UNKNOWN)
        )

        assertThat(matcher.findIndirectEvidence(unknownEffectVariantDriver)).isEmpty()
    }

    @Test
    fun `Should exclude direct hotspot matches`() {
        val directVariant = variantAnnotation(
            gene = "KRAS",
            chromosome = "12",
            position = 25245350,
            ref = "C",
            alt = "T"
        )
        val indirectVariant = variantAnnotation(
            gene = "KRAS",
            chromosome = "12",
            position = 25245351,
            ref = "C",
            alt = "A"
        )

        val directHotspot = TestServeMolecularFactory.hotspot(directVariant)
        val indirectHotspot = TestServeMolecularFactory.hotspot(indirectVariant)

        val directEvidence = createHotspotEvidence(
            hotspot = directHotspot,
            treatmentName = "Direct Treatment",
            drugClass = "KRAS Inhibitor"
        ).evidence
        val indirectEvidence = createHotspotEvidence(
            hotspot = indirectHotspot,
            treatmentName = "Indirect Treatment",
            drugClass = "KRAS Inhibitor"
        ).evidence

        val actinProteinEffect = GeneAlterationFactory.convertProteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
        val matcher = IndirectEvidenceMatcher.create(
            listOf(directEvidence, indirectEvidence),
            setOf(
                createKnownHotspot(directHotspot.firstVariant(), ProteinEffect.GAIN_OF_FUNCTION),
                createKnownHotspot(indirectHotspot.firstVariant(), ProteinEffect.GAIN_OF_FUNCTION)
            )
        )

        val patientVariant = TestVariantFactory.createMinimal().copy(
            gene = directVariant.gene(),
            proteinEffect = actinProteinEffect,
            chromosome = directVariant.chromosome(),
            position = directVariant.position(),
            ref = directVariant.ref(),
            alt = directVariant.alt()
        )

        assertThat(matcher.findIndirectEvidence(patientVariant)).containsExactly(indirectEvidence)
    }

    private fun variantAnnotation(
        gene: String,
        chromosome: String,
        position: Int,
        ref: String,
        alt: String
    ): VariantAnnotation {
        return TestServeMolecularFactory.createVariantAnnotation(
            gene = gene,
            chromosome = chromosome,
            position = position,
            ref = ref,
            alt = alt
        )
    }
}
