package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.actionability.TestIndirectEvidenceFactory.BRAF_T599R_VARIANT
import com.hartwig.actin.molecular.evidence.actionability.TestIndirectEvidenceFactory.BRAF_V600E_VARIANT
import com.hartwig.actin.molecular.evidence.actionability.TestIndirectEvidenceFactory.KRAS_G12V_VARIANT
import com.hartwig.actin.molecular.evidence.actionability.TestIndirectEvidenceFactory.KRAS_K5N_VARIANT
import com.hartwig.actin.molecular.evidence.actionability.TestIndirectEvidenceFactory.PIK3CA_E545K_VARIANT
import com.hartwig.actin.molecular.evidence.actionability.TestIndirectEvidenceFactory.createHotspotEvidence
import com.hartwig.actin.molecular.evidence.actionability.TestIndirectEvidenceFactory.createKnownHotspot
import com.hartwig.actin.molecular.evidence.actionability.TestIndirectEvidenceFactory.createVariant
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory.convertProteinEffect
import com.hartwig.serve.datamodel.efficacy.ImmutableEfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.ImmutableTreatment
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect as DriverProteinEffect

class IndirectEvidenceMatcherTest {

    @Test
    fun `create should include only non resistant related evidences`() {
        val resistantHotspot = TestServeMolecularFactory.hotspot(BRAF_V600E_VARIANT)
        val nonResistantHotspot = TestServeMolecularFactory.hotspot(KRAS_G12V_VARIANT)

        val resistantEvidence = TestServeEvidenceFactory.create(
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
                variants = setOf(BRAF_V600E_VARIANT)
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
            proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
        )

        val matcher = IndirectEvidenceMatcher.create(
            listOf(resistantEvidence, nonResistantEvidence),
            setOf(resistantKnownHotspot, nonResistantKnownHotspot)
        )

        val resistantVariantDriver = TestVariantFactory.createMinimal().copy(
            gene = BRAF_V600E_VARIANT.gene(),
            proteinEffect = convertProteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
        )
        val nonResistantVariantDriver = TestVariantFactory.createMinimal().copy(
            gene = KRAS_G12V_VARIANT.gene(),
            proteinEffect = convertProteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
        )

        assertThat(matcher.findIndirectEvidence(resistantVariantDriver)).isEmpty()
        assertThat(matcher.findIndirectEvidence(nonResistantVariantDriver))
            .containsExactly(nonResistantEvidence)
    }

    @Test
    fun `Should skip evidences with combined molecular criteria`() {
        val hotspot = TestServeMolecularFactory.hotspot(BRAF_V600E_VARIANT)

        val combinedCriterium = ImmutableMolecularCriterium.builder()
            .addHotspots(hotspot)
            .addAllGenes(TestServeMolecularFactory.createGeneCriterium(gene = BRAF_V600E_VARIANT.gene()).genes())
            .build()

        val baseEvidence = TestServeEvidenceFactory.create(
            molecularCriterium = combinedCriterium,
            treatment = "Combined Treatment"
        )
        val combinedEvidence = ImmutableEfficacyEvidence.builder()
            .from(baseEvidence)
            .treatment(
                ImmutableTreatment.builder()
                    .from(baseEvidence.treatment())
                    .treatmentApproachesDrugClass(listOf("BRAF Inhibitor"))
                    .build()
            )
            .build()

        val knownHotspot = createKnownHotspot(hotspot.firstVariant(), ProteinEffect.GAIN_OF_FUNCTION)
        val matcher = IndirectEvidenceMatcher.create(listOf(combinedEvidence), setOf(knownHotspot))

        val variantDriver = TestVariantFactory.createMinimal().copy(
            gene = BRAF_V600E_VARIANT.gene(),
            proteinEffect = GeneAlterationFactory.convertProteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
        )

        assertThat(matcher.findIndirectEvidence(variantDriver)).isEmpty()
    }

    @Test
    fun `Should exclude hotspots without gain or loss of function`() {
        val unknownHotspot = TestServeMolecularFactory.hotspot(PIK3CA_E545K_VARIANT)

        val unknownEffectEvidence = createHotspotEvidence(
            hotspot = unknownHotspot,
            treatmentName = "Generic Inhibitor Treatment",
            drugClass = "PIK3CA Inhibitor"
        ).evidence

        val unknownEffectKnownHotspot = createKnownHotspot(
            variant = unknownHotspot.firstVariant(),
            proteinEffect = ProteinEffect.UNKNOWN
        )

        val matcher = IndirectEvidenceMatcher.create(listOf(unknownEffectEvidence), setOf(unknownEffectKnownHotspot))

        val variantDriver = TestVariantFactory.createMinimal().copy(
            gene = PIK3CA_E545K_VARIANT.gene(),
            proteinEffect = GeneAlterationFactory.convertProteinEffect(ProteinEffect.UNKNOWN)
        )

        assertThat(matcher.findIndirectEvidence(variantDriver)).isEmpty()
    }

    @Test
    fun `Should exclude direct hotspot matches`() {
        val directHotspot = TestServeMolecularFactory.hotspot(BRAF_V600E_VARIANT)
        val indirectHotspot = TestServeMolecularFactory.hotspot(BRAF_T599R_VARIANT)

        val directEvidence = createHotspotEvidence(
            hotspot = directHotspot,
            treatmentName = "Direct Treatment",
            drugClass = "BRAF Inhibitor"
        ).evidence
        val indirectEvidence = createHotspotEvidence(
            hotspot = indirectHotspot,
            treatmentName = "Indirect Treatment",
            drugClass = "BRAF Inhibitor"
        ).evidence

        val matcher = IndirectEvidenceMatcher.create(
            listOf(directEvidence, indirectEvidence),
            setOf(
                createKnownHotspot(directHotspot.firstVariant(), ProteinEffect.GAIN_OF_FUNCTION),
                createKnownHotspot(indirectHotspot.firstVariant(), ProteinEffect.GAIN_OF_FUNCTION)
            )
        )

        val variantDriver = TestVariantFactory.createMinimal().copy(
            gene = BRAF_V600E_VARIANT.gene(),
            proteinEffect = convertProteinEffect(ProteinEffect.GAIN_OF_FUNCTION),
            chromosome = BRAF_V600E_VARIANT.chromosome(),
            position = BRAF_V600E_VARIANT.position(),
            ref = BRAF_V600E_VARIANT.ref(),
            alt = BRAF_V600E_VARIANT.alt()
        )

        assertThat(matcher.findIndirectEvidence(variantDriver)).containsExactly(indirectEvidence)
    }

    @Test
    fun `Should match predicted gain of function with gain of function`() {
        val predictedHotspot = TestServeMolecularFactory.hotspot(BRAF_T599R_VARIANT)
        val predictedEvidence = createHotspotEvidence(
            hotspot = predictedHotspot,
            treatmentName = "Treatment",
            drugClass = "BRAF Inhibitor"
        ).evidence

        val knownHotspot = createKnownHotspot(predictedHotspot.firstVariant(), ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
        val matcher = IndirectEvidenceMatcher.create(listOf(predictedEvidence), setOf(knownHotspot))

        val patientVariant = createVariant(BRAF_V600E_VARIANT, DriverProteinEffect.GAIN_OF_FUNCTION)

        assertThat(matcher.findIndirectEvidence(patientVariant)).containsExactly(predictedEvidence)
    }

    @Test
    fun `Should match predicted loss of function with loss of function`() {
        val predictedHotspot = TestServeMolecularFactory.hotspot(KRAS_G12V_VARIANT)
        val predictedEvidence = createHotspotEvidence(
            hotspot = predictedHotspot,
            treatmentName = "Treatment",
            drugClass = "KRAS Inhibitor"
        ).evidence

        val knownHotspot = createKnownHotspot(predictedHotspot.firstVariant(), ProteinEffect.LOSS_OF_FUNCTION_PREDICTED)
        val matcher = IndirectEvidenceMatcher.create(listOf(predictedEvidence), setOf(knownHotspot))

        val patientVariant = createVariant(KRAS_K5N_VARIANT, DriverProteinEffect.LOSS_OF_FUNCTION)

        assertThat(matcher.findIndirectEvidence(patientVariant)).containsExactly(predictedEvidence)
    }
}