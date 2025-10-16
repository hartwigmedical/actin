package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.efficacy.ImmutableEfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.ImmutableTreatment
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

        val resistantEvidence = TestServeEvidenceFactory.create(
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
                variants = setOf(resistantVariant)
            ),
            treatment = "Resistant Treatment"
        )
        val nonResistantEvidenceBase = TestServeEvidenceFactory.create(
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
                variants = setOf(nonResistantVariant)
            ),
            treatment = "Non Resistant Treatment"
        )
        val nonResistantEvidence = ImmutableEfficacyEvidence.builder()
            .from(nonResistantEvidenceBase)
            .treatment(
                ImmutableTreatment.builder()
                    .from(nonResistantEvidenceBase.treatment())
                    .treatmentApproachesDrugClass(listOf("KRAS Inhibitor"))
                    .build()
            )
            .build()

        val resistantKnownHotspot = knownHotspot(
            variant = resistantVariant,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            associatedWithDrugResistance = true
        )

        val nonResistantKnownHotspot = knownHotspot(
            variant = nonResistantVariant,
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

        val unknownEffectEvidenceBase = TestServeEvidenceFactory.create(
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
                variants = setOf(unknownEffectVariant)
            ),
            treatment = "Generic Inhibitor Treatment"
        )

        val unknownEffectEvidence = ImmutableEfficacyEvidence.builder()
            .from(unknownEffectEvidenceBase)
            .treatment(
                ImmutableTreatment.builder()
                    .from(unknownEffectEvidenceBase.treatment())
                    .treatmentApproachesDrugClass(listOf("PIK3CA Inhibitor"))
                    .build()
            )
            .build()

        val unknownEffectHotspot = knownHotspot(
            variant = unknownEffectVariant,
            proteinEffect = ProteinEffect.UNKNOWN
        )

        val matcher = IndirectEvidenceMatcher.create(listOf(unknownEffectEvidence), setOf(unknownEffectHotspot))

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

        val directEvidenceBase = TestServeEvidenceFactory.create(
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
                variants = setOf(directVariant)
            ),
            treatment = "Direct Treatment"
        )
        val directEvidence = ImmutableEfficacyEvidence.builder()
            .from(directEvidenceBase)
            .treatment(
                ImmutableTreatment.builder()
                    .from(directEvidenceBase.treatment())
                    .treatmentApproachesDrugClass(listOf("KRAS Inhibitor"))
                    .build()
            )
            .build()

        val indirectEvidenceBase = TestServeEvidenceFactory.create(
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
                variants = setOf(indirectVariant)
            ),
            treatment = "Indirect Treatment"
        )
        val indirectEvidence = ImmutableEfficacyEvidence.builder()
            .from(indirectEvidenceBase)
            .treatment(
                ImmutableTreatment.builder()
                    .from(indirectEvidenceBase.treatment())
                    .treatmentApproachesDrugClass(listOf("KRAS Inhibitor"))
                    .build()
            )
            .build()

        val actinProteinEffect = GeneAlterationFactory.convertProteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
        val matcher = IndirectEvidenceMatcher.create(
            listOf(directEvidence, indirectEvidence),
            setOf(knownHotspot(directVariant, ProteinEffect.GAIN_OF_FUNCTION), knownHotspot(indirectVariant, ProteinEffect.GAIN_OF_FUNCTION))
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

    private fun knownHotspot(
        variant: VariantAnnotation,
        proteinEffect: ProteinEffect,
        associatedWithDrugResistance: Boolean = false
    ) = TestServeKnownFactory.hotspotBuilder()
        .gene(variant.gene())
        .chromosome(variant.chromosome())
        .position(variant.position())
        .ref(variant.ref())
        .alt(variant.alt())
        .proteinEffect(proteinEffect)
        .associatedWithDrugResistance(associatedWithDrugResistance)
        .addSources(Knowledgebase.CKB)
        .build()
}
