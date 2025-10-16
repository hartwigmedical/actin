package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher
import com.hartwig.actin.molecular.evidence.actionability.CancerTypeApplicabilityResolver
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.GeneEffectKey
import com.hartwig.actin.molecular.evidence.actionability.IndirectEvidenceMatcher
import com.hartwig.actin.molecular.evidence.actionability.toGroupedProteinEffect
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.efficacy.ImmutableEfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.ImmutableTreatment
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val brafActionableHotspot = TestServeMolecularFactory.hotspot(
    TestServeMolecularFactory.createVariantAnnotation(
        gene = "BRAF", chromosome = "7", position = 140453136, ref = "T", alt = "A"
    )
)
private val relatedBrafActionableHotspot = TestServeMolecularFactory.hotspot(
    TestServeMolecularFactory.createVariantAnnotation(
        gene = "BRAF", chromosome = "7", position = 140453139, ref = "G", alt = "C"
    )
)
private val brafMolecularTestVariant = TestVariantFactory.createMinimal().copy(
    gene = "BRAF",
    chromosome = "7",
    position = 140453136,
    ref = "T",
    alt = "A",
    driverLikelihood = DriverLikelihood.HIGH,
    isReportable = true,
    proteinEffect = com.hartwig.actin.datamodel.molecular.driver.ProteinEffect.GAIN_OF_FUNCTION,
)

class EvidenceAnnotatorTest {

    @Test
    fun `Should annotate variants with evidence`() {
        val molecularCriterium = ImmutableMolecularCriterium.builder()
            .addAllHotspots(listOf(brafActionableHotspot))
            .build()
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = molecularCriterium,
        )

        val variant = TestMolecularFactory.createProperVariant()
        assertThat(variant.evidence.treatmentEvidence).isNotEmpty
        val indication = evidence.indication()
        val cancerTypeResolver = mockk<CancerTypeApplicabilityResolver> {
            every { resolve(indication) } returns CancerTypeMatchApplicability.SPECIFIC_TYPE
        }
        val clinicalEvidenceFactory = ClinicalEvidenceFactory(cancerTypeResolver, Gender.FEMALE)
        val trial = TestServeTrialFactory.create(
            anyMolecularCriteria = setOf(molecularCriterium),
            indications = setOf(indication),
            title = "title"
        )
        val knownHotspots: Set<KnownHotspot> = emptySet()  // TODO add hotspot
        val actionabilityMatcher = ActionabilityMatcher(listOf(evidence), listOf(trial), knownHotspots)

        val evidenceAnnotator = evidenceAnnotator(clinicalEvidenceFactory, actionabilityMatcher)

        val molecularTest = TestMolecularFactory.createMinimalPanelTest()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
            )

        val updatedTest = evidenceAnnotator.annotate(molecularTest)
        assertThat(updatedTest.drivers.variants).hasSize(1)

        val annotatedVariant = updatedTest.drivers.variants.first()

        assertThat(clearEvidence(annotatedVariant)).isEqualTo(clearEvidence(brafMolecularTestVariant))
        assertThat(annotatedVariant.evidence.treatmentEvidence.first().treatment).isEqualTo("treatment")
        assertThat(annotatedVariant.evidence.eligibleTrials.first().title).isEqualTo("title")
    }

    @Test
    fun `Should not fail annotating variants without evidence`() {
        val tumorDoids = setOf("DOID:162", "DOID:14502")
        val cancerTypeResolver = CancerTypeApplicabilityResolver(tumorDoids)
        val clinicalEvidenceFactory = ClinicalEvidenceFactory(cancerTypeResolver, Gender.FEMALE)

        val evidenceAnnotator = evidenceAnnotator(
            clinicalEvidenceFactory,
            ActionabilityMatcher(emptyList(), emptyList(), emptySet())
        )

        val molecularTest = TestMolecularFactory.createMinimalPanelTest()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                    variants = listOf(
                        TestMolecularFactory.createProperVariant().copy(
                            evidence = TestClinicalEvidenceFactory.createEmpty()
                        )
                    )
                )
            )

        val updatedTest = evidenceAnnotator.annotate(molecularTest)
        assertThat(updatedTest.drivers.variants).hasSize(1)
        assertThat(updatedTest.drivers.variants).isEqualTo(molecularTest.drivers.variants)
    }

    @Test
    fun `Should annotate variants with indirect evidence`() {
        val criterium = ImmutableMolecularCriterium.builder()
            .addAllHotspots(listOf(relatedBrafActionableHotspot))
            .build()
        val baseEvidence = TestServeEvidenceFactory.create(
            molecularCriterium = criterium,
            treatment = "Related Treatment"
        )
        val evidence = ImmutableEfficacyEvidence.builder()
            .from(baseEvidence)
            .treatment(
                ImmutableTreatment.builder()
                    .from(baseEvidence.treatment())
                    .treatmentApproachesDrugClass(listOf("Generic Inhibitor"))
                    .build()
            )
            .build()

        val indication = evidence.indication()
        val cancerTypeResolver = mockk<CancerTypeApplicabilityResolver> {
            every { resolve(indication) } returns CancerTypeMatchApplicability.SPECIFIC_TYPE
        }
        val clinicalEvidenceFactory = ClinicalEvidenceFactory(cancerTypeResolver, patientGender = null)
        val relatedMatcher = IndirectEvidenceMatcher(
            mapOf(GeneEffectKey(brafMolecularTestVariant.gene, brafMolecularTestVariant.proteinEffect.toGroupedProteinEffect()) to setOf(evidence))
        )
        val knowHotspot = TestServeKnownFactory.hotspotBuilder()
            .gene(relatedBrafActionableHotspot.variants().first().gene())
            .chromosome(relatedBrafActionableHotspot.variants().first().chromosome())
            .position(relatedBrafActionableHotspot.variants().first().position())
            .ref(relatedBrafActionableHotspot.variants().first().ref())
            .alt(relatedBrafActionableHotspot.variants().first().alt())
            .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
            .build()

        val actionabilityMatcher = ActionabilityMatcher(listOf(evidence), emptyList(), setOf(knowHotspot))

        val evidenceAnnotator = evidenceAnnotator(clinicalEvidenceFactory, actionabilityMatcher)

        val molecularTest = TestMolecularFactory.createMinimalPanelTest()
            .copy(drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant)))

        val updatedTest = evidenceAnnotator.annotate(molecularTest)
        val annotatedVariant = updatedTest.drivers.variants.first()

        assertThat(annotatedVariant.evidence.treatmentEvidence).anySatisfy { assertThat(it.treatment).isEqualTo("Related Treatment") }
    }

    private fun evidenceAnnotator(
        clinicalEvidenceFactory: ClinicalEvidenceFactory,
        actionabilityMatcher: ActionabilityMatcher
    ) = EvidenceAnnotator(clinicalEvidenceFactory, actionabilityMatcher) { input, drivers, characteristics ->
        input.copy(drivers = drivers, characteristics = characteristics)
    }

    private fun clearEvidence(variant: Variant): Variant {
        return variant.copy(
            evidence = TestClinicalEvidenceFactory.createEmpty()
        )
    }
}