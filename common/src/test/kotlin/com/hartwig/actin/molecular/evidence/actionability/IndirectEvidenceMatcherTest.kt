package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.serve.datamodel.ImmutableServeRecord
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.efficacy.ImmutableEfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.ImmutableTreatment
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
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

        val resistantKnownHotspot = TestServeKnownFactory.hotspotBuilder()
            .gene(resistantVariant.gene())
            .chromosome(resistantVariant.chromosome())
            .position(resistantVariant.position())
            .ref(resistantVariant.ref())
            .alt(resistantVariant.alt())
            .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
            .associatedWithDrugResistance(true)
            .addSources(Knowledgebase.CKB)
            .build()

        val nonResistantKnownHotspot = TestServeKnownFactory.hotspotBuilder()
            .gene(nonResistantVariant.gene())
            .chromosome(nonResistantVariant.chromosome())
            .position(nonResistantVariant.position())
            .ref(nonResistantVariant.ref())
            .alt(nonResistantVariant.alt())
            .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
            .associatedWithDrugResistance(false)
            .addSources(Knowledgebase.CKB)
            .build()

        val serveRecord = ImmutableServeRecord.builder()
            .knownEvents(
                ImmutableKnownEvents.builder()
                    .addHotspots(resistantKnownHotspot)
                    .addHotspots(nonResistantKnownHotspot)
                    .build()
            )
            .addEvidences(resistantEvidence)
            .addEvidences(nonResistantEvidence)
            .build()

        val actinProteinEffect = GeneAlterationFactory.convertProteinEffect(ProteinEffect.GAIN_OF_FUNCTION)

        val matcher = IndirectEvidenceMatcher.create(serveRecord)

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

        val unknownEffectHotspot = TestServeKnownFactory.hotspotBuilder()
            .gene(unknownEffectVariant.gene())
            .chromosome(unknownEffectVariant.chromosome())
            .position(unknownEffectVariant.position())
            .ref(unknownEffectVariant.ref())
            .alt(unknownEffectVariant.alt())
            .proteinEffect(ProteinEffect.UNKNOWN)
            .associatedWithDrugResistance(false)
            .addSources(Knowledgebase.CKB)
            .build()

        val serveRecord = ImmutableServeRecord.builder()
            .knownEvents(
                ImmutableKnownEvents.builder()
                    .addHotspots(unknownEffectHotspot)
                    .build()
            )
            .addEvidences(unknownEffectEvidence)
            .build()

        val matcher = IndirectEvidenceMatcher.create(serveRecord)

        val unknownEffectVariantDriver = TestVariantFactory.createMinimal().copy(
            gene = unknownEffectVariant.gene(),
            proteinEffect = GeneAlterationFactory.convertProteinEffect(ProteinEffect.UNKNOWN)
        )

        assertThat(matcher.findIndirectEvidence(unknownEffectVariantDriver)).isEmpty()
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
