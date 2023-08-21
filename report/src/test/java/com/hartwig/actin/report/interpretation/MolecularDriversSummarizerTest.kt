package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.Virus
import com.hartwig.actin.molecular.datamodel.driver.VirusType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import org.junit.Assert
import org.junit.Test
import java.util.Set
import java.util.stream.Collectors
import java.util.stream.Stream

class MolecularDriversSummarizerTest {
    @Test
    fun shouldReturnKeyVariants() {
        val variants = Set.of(
            variant(EXPECTED_GENE, DriverLikelihood.HIGH, true),
            variant("non-reportable", DriverLikelihood.HIGH, false),
            variant("medium likelihood", DriverLikelihood.MEDIUM, true)
        )
        val molecularDrivers: MolecularDrivers = ImmutableMolecularDrivers.builder().addAllVariants(variants).build()
        assertExpectedStreamResult(summarizer(molecularDrivers).keyGenesWithVariants())
    }

    @Test
    fun shouldReturnKeyAmplifiedGenesAndIndicatePartialAmplifications() {
        val partialAmpGene = "partial amp"
        val fullAmpGene = "full amp"
        val copyNumbers = Set.of(
            copyNumber(CopyNumberType.FULL_GAIN, fullAmpGene, DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.PARTIAL_GAIN, partialAmpGene, DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.LOSS, "loss", DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.FULL_GAIN, "low", DriverLikelihood.LOW, true),
            copyNumber(CopyNumberType.FULL_GAIN, "non-reportable", DriverLikelihood.HIGH, false)
        )
        val molecularDrivers: MolecularDrivers = ImmutableMolecularDrivers.builder().addAllCopyNumbers(copyNumbers).build()
        val amplifiedGenes = summarizer(molecularDrivers).keyAmplifiedGenes().collect(Collectors.toSet())
        Assert.assertEquals(2, amplifiedGenes.size.toLong())
        Assert.assertTrue(amplifiedGenes.contains("$partialAmpGene (partial)"))
        Assert.assertTrue(amplifiedGenes.contains(fullAmpGene))
    }

    @Test
    fun shouldReturnKeyDeletedGenes() {
        val copyNumbers = Set.of(
            copyNumber(CopyNumberType.FULL_GAIN, "full amp", DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.PARTIAL_GAIN, "partial amp", DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.LOSS, EXPECTED_GENE, DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.LOSS, "low", DriverLikelihood.LOW, true),
            copyNumber(CopyNumberType.LOSS, "non-reportable", DriverLikelihood.HIGH, false)
        )
        val molecularDrivers: MolecularDrivers = ImmutableMolecularDrivers.builder().addAllCopyNumbers(copyNumbers).build()
        assertExpectedStreamResult(summarizer(molecularDrivers).keyDeletedGenes())
    }

    @Test
    fun shouldReturnKeyHomozygouslyDisruptedGenes() {
        val homozygousDisruptions = Set.of(
            homozygousDisruption(EXPECTED_GENE, DriverLikelihood.HIGH, true),
            homozygousDisruption("non-reportable", DriverLikelihood.HIGH, false),
            homozygousDisruption("medium likelihood", DriverLikelihood.MEDIUM, true)
        )
        val molecularDrivers: MolecularDrivers =
            ImmutableMolecularDrivers.builder().addAllHomozygousDisruptions(homozygousDisruptions).build()
        assertExpectedStreamResult(summarizer(molecularDrivers).keyHomozygouslyDisruptedGenes())
    }

    @Test
    fun shouldReturnKeyFusions() {
        val fusions = Set.of(
            fusion(EXPECTED_GENE, DriverLikelihood.HIGH, true),
            fusion("non-reportable", DriverLikelihood.HIGH, false),
            fusion("medium likelihood", DriverLikelihood.MEDIUM, true)
        )
        val molecularDrivers: MolecularDrivers = ImmutableMolecularDrivers.builder().addAllFusions(fusions).build()
        assertExpectedStreamResult(summarizer(molecularDrivers).keyFusionEvents())
    }

    @Test
    fun shouldReturnKeyViruses() {
        val viruses = Set.of(
            virus("virus", DriverLikelihood.HIGH, true),
            virus("non-reportable", DriverLikelihood.HIGH, false),
            virus("medium likelihood", DriverLikelihood.MEDIUM, true)
        )
        val molecularDrivers: MolecularDrivers = ImmutableMolecularDrivers.builder().addAllViruses(viruses).build()
        val keyViruses = summarizer(molecularDrivers).keyVirusEvents().collect(Collectors.toSet())
        Assert.assertEquals(1, keyViruses.size.toLong())
        Assert.assertTrue(keyViruses.contains(VirusType.MERKEL_CELL_VIRUS.toString() + " (" + VIRUS_INTEGRATIONS + " integrations detected)"))
    }

    @Test
    fun shouldReturnActionableEventsThatAreNotKeyDrivers() {
        val externalEvidence = TestActionableEvidenceFactory.withExternalEligibleTrial("external")
        val approvedTreatment = TestActionableEvidenceFactory.withApprovedTreatment("approved")
        val cohorts: List<EvaluatedCohort?> = listOf<EvaluatedCohort>(
            EvaluatedCohortTestFactory.builder()
                .isPotentiallyEligible(true)
                .isOpen(true)
                .addMolecularEvents(
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
                .build()
        )
        val variants = Set.of(
            variant("key variant", DriverLikelihood.HIGH, true, externalEvidence),
            variant("expected non-reportable variant", DriverLikelihood.HIGH, false, approvedTreatment),
            variant("expected medium likelihood variant", DriverLikelihood.MEDIUM, true),
            variant("no evidence", DriverLikelihood.MEDIUM, true)
        )
        val copyNumbers = Set.of(
            copyNumber(CopyNumberType.FULL_GAIN, "key gain", DriverLikelihood.HIGH, true),
            copyNumber(CopyNumberType.PARTIAL_GAIN, "expected amplification", null, false),
            copyNumber(CopyNumberType.LOSS, "expected loss", DriverLikelihood.HIGH, false),
            copyNumber(CopyNumberType.FULL_GAIN, "no evidence", DriverLikelihood.LOW, true)
        )
        val homozygousDisruptions = Set.of(
            homozygousDisruption("key HD", DriverLikelihood.HIGH, true, approvedTreatment),
            homozygousDisruption("expected non-reportable HD", DriverLikelihood.HIGH, false, approvedTreatment),
            homozygousDisruption("expected null likelihood HD", null, true, externalEvidence)
        )
        val disruptions = Set.of(
            disruption("expected key disruption", DriverLikelihood.HIGH, true),
            disruption("expected non-reportable disruption", DriverLikelihood.LOW, false, approvedTreatment),
            disruption("no evidence disruption", DriverLikelihood.MEDIUM, false)
        )
        val fusions = Set.of(
            fusion("key fusion", DriverLikelihood.HIGH, true, externalEvidence),
            fusion("expected non-reportable fusion", DriverLikelihood.HIGH, false),
            fusion("expected medium likelihood fusion", DriverLikelihood.MEDIUM, true, approvedTreatment)
        )
        val viruses = Set.of(
            virus("expected low likelihood virus", DriverLikelihood.LOW, true),
            virus("expected non-reportable virus", DriverLikelihood.LOW, false),
            virus("key virus", DriverLikelihood.HIGH, true)
        )
        val molecularDrivers: MolecularDrivers = ImmutableMolecularDrivers.builder()
            .addAllVariants(variants)
            .addAllCopyNumbers(copyNumbers)
            .addAllHomozygousDisruptions(homozygousDisruptions)
            .addAllDisruptions(disruptions)
            .addAllFusions(fusions)
            .addAllViruses(viruses)
            .build()
        val summarizer = MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(molecularDrivers, cohorts)
        val otherActionableEvents = summarizer.actionableEventsThatAreNotKeyDrivers().collect(Collectors.toSet())
        Assert.assertEquals(12, otherActionableEvents.size.toLong())
        Assert.assertTrue(otherActionableEvents.stream().allMatch { event: String -> event.startsWith("expected") })
    }

    companion object {
        private const val EXPECTED_GENE = "found"
        private const val VIRUS_INTEGRATIONS = 3
        private fun variant(
            name: String,
            driverLikelihood: DriverLikelihood,
            isReportable: Boolean,
            evidence: ActionableEvidence = TestActionableEvidenceFactory.createEmpty()
        ): Variant {
            return TestVariantFactory.builder()
                .gene(name)
                .event(name)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .evidence(evidence)
                .build()
        }

        private fun copyNumber(type: CopyNumberType, name: String, driverLikelihood: DriverLikelihood?, isReportable: Boolean): CopyNumber {
            return TestCopyNumberFactory.builder()
                .type(type)
                .gene(name)
                .event(name)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .build()
        }

        private fun homozygousDisruption(
            name: String, driverLikelihood: DriverLikelihood?, isReportable: Boolean,
            evidence: ActionableEvidence = TestActionableEvidenceFactory.createEmpty()
        ): HomozygousDisruption {
            return TestHomozygousDisruptionFactory.builder()
                .gene(name)
                .event(name)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .evidence(evidence)
                .build()
        }

        private fun disruption(
            name: String, driverLikelihood: DriverLikelihood, isReportable: Boolean,
            evidence: ActionableEvidence = TestActionableEvidenceFactory.createEmpty()
        ): Disruption {
            return TestDisruptionFactory.builder()
                .gene(name)
                .event(name)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .evidence(evidence)
                .build()
        }

        private fun fusion(
            event: String,
            driverLikelihood: DriverLikelihood,
            isReportable: Boolean,
            evidence: ActionableEvidence = TestActionableEvidenceFactory.createEmpty()
        ): Fusion {
            return TestFusionFactory.builder()
                .event(event)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .evidence(evidence)
                .build()
        }

        private fun virus(event: String, driverLikelihood: DriverLikelihood, isReportable: Boolean): Virus {
            return TestVirusFactory.builder()
                .event(event)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .type(VirusType.MERKEL_CELL_VIRUS)
                .integrations(VIRUS_INTEGRATIONS)
                .build()
        }

        private fun summarizer(molecularDrivers: MolecularDrivers): MolecularDriversSummarizer {
            return MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(molecularDrivers, emptyList())
        }

        private fun assertExpectedStreamResult(keyEntryStream: Stream<String>) {
            val keyEntries = keyEntryStream.collect(Collectors.toSet())
            Assert.assertEquals(1, keyEntries.size.toLong())
            Assert.assertTrue(keyEntries.contains(EXPECTED_GENE))
        }
    }
}