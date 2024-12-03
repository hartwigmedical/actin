package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVirusFactory
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType
import com.hartwig.actin.report.interpretation.InterpretedCohortTestFactory.interpretedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val EXPECTED_GENE = "found"
private const val VIRUS_INTEGRATIONS = 3

class DriversSummarizerTest {

    private val minimalDrivers = TestMolecularFactory.createMinimalTestMolecularRecord().drivers
    
    @Test
    fun `Should return key variants`() {
        val variants = listOf(
            variant(EXPECTED_GENE, DriverLikelihood.HIGH, true),
            variant("non-reportable", DriverLikelihood.HIGH, false),
            variant("medium likelihood", DriverLikelihood.MEDIUM, true)
        )
        val molecularDrivers = minimalDrivers.copy(variants = variants)
        assertExpectedListResult(summarizer(molecularDrivers).keyVariants())
    }

    @Test
    fun `Should return key amplified genes and indicate partial amplifications`() {
        val partialAmpGene = "partial amp"
        val fullAmpGene = "full amp"
        val copyNumbers = listOf(
            copyNumber(CopyNumberType.FULL_GAIN, fullAmpGene, DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.PARTIAL_GAIN, partialAmpGene, DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.LOSS, "loss", DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.FULL_GAIN, "low", DriverLikelihood.LOW, true),
            copyNumber(CopyNumberType.FULL_GAIN, "non-reportable", DriverLikelihood.HIGH, false)
        )
        val molecularDrivers = minimalDrivers.copy(copyNumbers = copyNumbers)
        val amplifiedGenes = summarizer(molecularDrivers).keyAmplifiedGenes().toSet()
        assertThat(amplifiedGenes).containsExactlyInAnyOrder("$partialAmpGene (partial)", fullAmpGene)
    }

    @Test
    fun `Should return key deleted genes`() {
        val copyNumbers = listOf(
            copyNumber(CopyNumberType.FULL_GAIN, "full amp", DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.PARTIAL_GAIN, "partial amp", DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.LOSS, EXPECTED_GENE, DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.LOSS, "low", DriverLikelihood.LOW, true),
            copyNumber(CopyNumberType.LOSS, "non-reportable", DriverLikelihood.HIGH, false)
        )
        val molecularDrivers = minimalDrivers.copy(copyNumbers = copyNumbers)
        assertExpectedListResult(summarizer(molecularDrivers).keyDeletedGenes())
    }

    @Test
    fun `Should return key homozygously disrupted genes`() {
        val homozygousDisruptions = listOf(
            homozygousDisruption(EXPECTED_GENE, DriverLikelihood.HIGH, true),
            homozygousDisruption("non-reportable", DriverLikelihood.HIGH, false),
            homozygousDisruption("medium likelihood", DriverLikelihood.MEDIUM, true)
        )
        val molecularDrivers = minimalDrivers.copy(homozygousDisruptions = homozygousDisruptions)
        assertExpectedListResult(summarizer(molecularDrivers).keyHomozygouslyDisruptedGenes())
    }

    @Test
    fun `Should return key fusions`() {
        val fusions = listOf(
            fusion(EXPECTED_GENE, DriverLikelihood.HIGH, true),
            fusion("non-reportable", DriverLikelihood.HIGH, false),
            fusion("medium likelihood", DriverLikelihood.MEDIUM, true)
        )
        val molecularDrivers = minimalDrivers.copy(fusions = fusions)
        assertExpectedListResult(summarizer(molecularDrivers).keyFusionEvents())
    }

    @Test
    fun `Should return key viruses`() {
        val viruses = listOf(
            virus("virus", DriverLikelihood.HIGH, true),
            virus("non-reportable", DriverLikelihood.HIGH, false),
            virus("medium likelihood", DriverLikelihood.MEDIUM, true)
        )
        val molecularDrivers = minimalDrivers.copy(viruses = viruses)
        val keyViruses = summarizer(molecularDrivers).keyVirusEvents().toSet()
        assertThat(keyViruses).containsExactly("virus ($VIRUS_INTEGRATIONS int. detected)")
    }

    @Test
    fun `Should return actionable events that are not key drivers`() {
        val externalEvidence = TestClinicalEvidenceFactory.withEligibleTrial(TestExternalTrialFactory.createTestTrial())
        val approvedTreatment = TestClinicalEvidenceFactory.withApprovedTreatment("approved")

        val cohorts = listOf(
            interpretedCohort(
                isPotentiallyEligible = true,
                isOpen = true,
                molecularEvents = setOf(
                    "expected medium likelihood variant",
                    "expected low likelihood virus",
                    "expected non-reportable virus",
                    "key virus",
                    "key gain",
                    "expected amplification",
                    "expected loss",
                    "expected key disruption",
                    "expected non-reportable fusion"
                )
            )
        )

        val variants = listOf(
            variant("key variant", DriverLikelihood.HIGH, true, externalEvidence),
            variant("expected non-reportable variant", DriverLikelihood.HIGH, false, approvedTreatment),
            variant("expected medium likelihood variant", DriverLikelihood.MEDIUM, true),
            variant("no evidence", DriverLikelihood.MEDIUM, true)
        )
        val copyNumbers = listOf(
            copyNumber(CopyNumberType.FULL_GAIN, "key gain", DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.PARTIAL_GAIN, "expected amplification", null, false),
            copyNumber(CopyNumberType.LOSS, "expected loss", DriverLikelihood.HIGH, false),
            copyNumber(CopyNumberType.FULL_GAIN, "no evidence", DriverLikelihood.LOW, true)
        )
        val homozygousDisruptions = listOf(
            homozygousDisruption("key HD", DriverLikelihood.HIGH, true, approvedTreatment),
            homozygousDisruption("expected non-reportable HD", DriverLikelihood.HIGH, false, approvedTreatment),
            homozygousDisruption("expected null likelihood HD", null, true, externalEvidence)
        )
        val disruptions = listOf(
            disruption("expected key disruption", DriverLikelihood.HIGH, true),
            disruption("expected non-reportable disruption", DriverLikelihood.LOW, false, approvedTreatment),
            disruption("no evidence disruption", DriverLikelihood.MEDIUM, false)
        )
        val fusions = listOf(
            fusion("key fusion", DriverLikelihood.HIGH, true, externalEvidence),
            fusion("expected non-reportable fusion", DriverLikelihood.HIGH, false),
            fusion("expected medium likelihood fusion", DriverLikelihood.MEDIUM, true, approvedTreatment)
        )
        val viruses = listOf(
            virus("expected low likelihood virus", DriverLikelihood.LOW, true),
            virus("expected non-reportable virus", DriverLikelihood.LOW, false),
            virus("key virus", DriverLikelihood.HIGH, true)
        )

        val drivers = Drivers(
            variants = variants,
            copyNumbers = copyNumbers,
            homozygousDisruptions = homozygousDisruptions,
            disruptions = disruptions,
            fusions = fusions,
            viruses = viruses
        )

        val summarizer = MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(drivers, cohorts)
        val otherActionableEvents = summarizer.actionableEventsThatAreNotKeyDrivers().map(Driver::event).distinct().toSet()

        assertThat(otherActionableEvents).hasSize(12)
        assertThat(otherActionableEvents).allSatisfy { it.startsWith("expected") }
    }

    private fun variant(
        name: String,
        driverLikelihood: DriverLikelihood,
        isReportable: Boolean,
        evidence: ClinicalEvidence = TestClinicalEvidenceFactory.createEmpty()
    ): Variant {
        return TestVariantFactory.createMinimal().copy(
            gene = name,
            event = name,
            driverLikelihood = driverLikelihood,
            isReportable = isReportable,
            evidence = evidence
        )
    }

    private fun copyNumber(type: CopyNumberType, name: String, driverLikelihood: DriverLikelihood?, isReportable: Boolean): CopyNumber {
        return TestCopyNumberFactory.createMinimal().copy(
            type = type,
            gene = name,
            event = name,
            driverLikelihood = driverLikelihood,
            isReportable = isReportable
        )
    }

    private fun homozygousDisruption(
        name: String, driverLikelihood: DriverLikelihood?, isReportable: Boolean,
        evidence: ClinicalEvidence = TestClinicalEvidenceFactory.createEmpty()
    ): HomozygousDisruption {
        return TestHomozygousDisruptionFactory.createMinimal().copy(
            gene = name,
            event = name,
            driverLikelihood = driverLikelihood,
            isReportable = isReportable,
            evidence = evidence
        )
    }

    private fun disruption(
        name: String, driverLikelihood: DriverLikelihood, isReportable: Boolean,
        evidence: ClinicalEvidence = TestClinicalEvidenceFactory.createEmpty()
    ): Disruption {
        return TestDisruptionFactory.createMinimal().copy(
            gene = name,
            event = name,
            driverLikelihood = driverLikelihood,
            isReportable = isReportable,
            evidence = evidence
        )
    }

    private fun fusion(
        event: String,
        driverLikelihood: DriverLikelihood,
        isReportable: Boolean,
        evidence: ClinicalEvidence = TestClinicalEvidenceFactory.createEmpty()
    ): Fusion {
        return TestFusionFactory.createMinimal().copy(
            event = event,
            driverLikelihood = driverLikelihood,
            isReportable = isReportable,
            evidence = evidence
        )
    }

    private fun virus(event: String, driverLikelihood: DriverLikelihood, isReportable: Boolean): Virus {
        return TestVirusFactory.createMinimal().copy(
            event = event,
            driverLikelihood = driverLikelihood,
            isReportable = isReportable,
            type = VirusType.MERKEL_CELL_VIRUS,
            integrations = VIRUS_INTEGRATIONS
        )
    }

    private fun summarizer(drivers: Drivers): MolecularDriversSummarizer {
        return MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(drivers, emptyList())
    }

    private fun assertExpectedListResult(keyEntryList: List<String>) {
        val keyEntries = keyEntryList.distinct()
        assertThat(keyEntries).containsExactly(EXPECTED_GENE)
    }
}