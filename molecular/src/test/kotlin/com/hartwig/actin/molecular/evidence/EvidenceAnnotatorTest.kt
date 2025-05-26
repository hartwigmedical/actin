package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.CancerTypeApplicabilityResolver
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.CombinedEvidenceMatcher
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val brafActionableHotspot =
    TestServeMolecularFactory.hotspot(TestServeMolecularFactory.createVariantAnnotation(
        gene = "BRAF",
        chromosome = "7",
        position = 140453136,
        ref = "T",
        alt = "A")
    )

private val brafMolecularTestVariant = TestVariantFactory.createMinimal()
    .copy(
        gene = "BRAF",
        chromosome = "7",
        position = 140453136,
        ref = "T",
        alt = "A",
        driverLikelihood = DriverLikelihood.HIGH,
        isReportable = true,
        evidence = TestClinicalEvidenceFactory.withApprovedTreatment("Vemurafenib")
    )

private val properVariant = TestMolecularFactory.createProperVariant()

class EvidenceAnnotatorTest {

    @Test
    fun `Should annotate variants with evidence`() {

        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafActionableHotspot))
                .build(),
        )

        val variant = TestMolecularFactory.createProperVariant()
        assertThat(variant.evidence.treatmentEvidence).isNotEmpty

        val tumorDoids = setOf("DOID:162", "DOID:14502")
        val cancerTypeResolver = CancerTypeApplicabilityResolver(tumorDoids)
        val clinicalEvidenceFactory = ClinicalEvidenceFactory(cancerTypeResolver)
//        val combinedEvidenceMatcher = CombinedEvidenceMatcherFactory.create(serveRecord)
        val combinedEvidenceMatcher = CombinedEvidenceMatcher(listOf(evidence))

        val evidenceAnnotator = EvidenceAnnotator(
            clinicalEvidenceFactory,
            combinedEvidenceMatcher
        )

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
            )

        val updatedTest = evidenceAnnotator.annotate(molecularTest)
        assertThat(updatedTest.drivers.variants).hasSize(1)

        val annotatedVariant = updatedTest.drivers.variants.first()

        assertThat(clearEvidence(annotatedVariant)).isEqualTo(clearEvidence(brafMolecularTestVariant))
        assertThat(annotatedVariant.evidence.treatmentEvidence.first().treatment).isEqualTo("treatment")  // replaced Vemurafenib
    }

    fun clearEvidence(variant: Variant): Variant {
        return variant.copy(
            evidence = TestClinicalEvidenceFactory.createEmpty()
        )
    }

    @Test
    fun `Should not fail annotating variants without evidence`() {
        val tumorDoids = setOf("DOID:162", "DOID:14502")
        val cancerTypeResolver = CancerTypeApplicabilityResolver(tumorDoids)
        val clinicalEvidenceFactory = ClinicalEvidenceFactory(cancerTypeResolver)

        val evidenceAnnotator = EvidenceAnnotator(
            clinicalEvidenceFactory,
            CombinedEvidenceMatcher(emptyList())
        )

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(properVariant.copy(
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )))
            )

        val updatedTest = evidenceAnnotator.annotate(molecularTest)
        assertThat(updatedTest.drivers.variants).hasSize(1)
        assertThat(updatedTest.drivers.variants).isEqualTo(molecularTest.drivers.variants)
    }
}