package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.panel.PanelRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher
import com.hartwig.actin.molecular.evidence.actionability.CancerTypeApplicabilityResolver
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceFactory
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.Test

private val brafActionableHotspot = TestServeMolecularFactory.hotspot(
    TestServeMolecularFactory.createVariantAnnotation(
        gene = "BRAF", chromosome = "7", position = 140453136, ref = "T", alt = "A"
    )
)
private val brafMolecularTestVariant = TestVariantFactory.createMinimal().copy(
    gene = "BRAF",
    chromosome = "7",
    position = 140453136,
    ref = "T",
    alt = "A",
    driverLikelihood = DriverLikelihood.HIGH,
    isReportable = true
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
        Assertions.assertThat(variant.evidence.treatmentEvidence).isNotEmpty
        val indication = evidence.indication()
        val cancerTypeResolver = mockk<CancerTypeApplicabilityResolver> {
            every { resolve(indication) } returns CancerTypeMatchApplicability.SPECIFIC_TYPE
        }
        val clinicalEvidenceFactory = ClinicalEvidenceFactory(cancerTypeResolver)
        val trial = TestServeTrialFactory.create(
            anyMolecularCriteria = setOf(molecularCriterium),
            indications = setOf(indication),
            title = "title"
        )
        val actionabilityMatcher = ActionabilityMatcher(listOf(evidence), listOf(trial))

        val evidenceAnnotator = evidenceAnnotator(clinicalEvidenceFactory, actionabilityMatcher)

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
            )

        val updatedTest = evidenceAnnotator.annotate(molecularTest)
        Assertions.assertThat(updatedTest.drivers.variants).hasSize(1)

        val annotatedVariant = updatedTest.drivers.variants.first()

        Assertions.assertThat(clearEvidence(annotatedVariant)).isEqualTo(clearEvidence(brafMolecularTestVariant))
        Assertions.assertThat(annotatedVariant.evidence.treatmentEvidence.first().treatment).isEqualTo("treatment")  // replaced Vemurafenib
        Assertions.assertThat(annotatedVariant.evidence.eligibleTrials.first().title).isEqualTo("title")
    }

    @Test
    fun `Should not fail annotating variants without evidence`() {
        val tumorDoids = setOf("DOID:162", "DOID:14502")
        val cancerTypeResolver = CancerTypeApplicabilityResolver(tumorDoids)
        val clinicalEvidenceFactory = ClinicalEvidenceFactory(cancerTypeResolver)

        val evidenceAnnotator = evidenceAnnotator(
            clinicalEvidenceFactory,
            ActionabilityMatcher(emptyList(), emptyList())
        )

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
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
        Assertions.assertThat(updatedTest.drivers.variants).hasSize(1)
        Assertions.assertThat(updatedTest.drivers.variants).isEqualTo(molecularTest.drivers.variants)
    }

    private fun evidenceAnnotator(
        clinicalEvidenceFactory: ClinicalEvidenceFactory,
        actionabilityMatcher: ActionabilityMatcher
    ) = EvidenceAnnotator<PanelRecord>(
        clinicalEvidenceFactory,
        actionabilityMatcher
    ) { input, drivers, characteristics ->
        input.copy(drivers = drivers, characteristics = characteristics)
    }

    private fun clearEvidence(variant: Variant): Variant {
        return variant.copy(
            evidence = TestClinicalEvidenceFactory.createEmpty()
        )
    }
}