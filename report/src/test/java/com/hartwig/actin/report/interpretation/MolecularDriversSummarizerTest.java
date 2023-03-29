package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.driver.VirusType;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;

import org.junit.Test;

public class MolecularDriversSummarizerTest {

    private static final String EXPECTED_GENE = "found";
    private static final int VIRUS_INTEGRATIONS = 3;

    @Test
    public void shouldReturnKeyVariants() {
        Set<Variant> variants = Set.of(variant(EXPECTED_GENE, DriverLikelihood.HIGH, true),
                variant("non-reportable", DriverLikelihood.HIGH, false),
                variant("medium likelihood", DriverLikelihood.MEDIUM, true));

        MolecularDrivers molecularDrivers = ImmutableMolecularDrivers.builder().addAllVariants(variants).build();
        assertExpectedStreamResult(summarizer(molecularDrivers).keyGenesWithVariants());
    }

    @Test
    public void shouldReturnKeyAmplifiedGenesAndIndicatePartialAmplifications() {
        String partialAmpGene = "partial amp";
        String fullAmpGene = "full amp";

        Set<CopyNumber> copyNumbers = Set.of(copyNumber(CopyNumberType.FULL_GAIN, fullAmpGene, DriverLikelihood.HIGH, true),
                copyNumber(CopyNumberType.PARTIAL_GAIN, partialAmpGene, DriverLikelihood.HIGH, true),
                copyNumber(CopyNumberType.LOSS, "loss", DriverLikelihood.HIGH, true),
                copyNumber(CopyNumberType.FULL_GAIN, "low", DriverLikelihood.LOW, true),
                copyNumber(CopyNumberType.FULL_GAIN, "non-reportable", DriverLikelihood.HIGH, false));

        MolecularDrivers molecularDrivers = ImmutableMolecularDrivers.builder().addAllCopyNumbers(copyNumbers).build();

        Set<String> amplifiedGenes = summarizer(molecularDrivers).keyAmplifiedGenes().collect(Collectors.toSet());
        assertEquals(2, amplifiedGenes.size());
        assertTrue(amplifiedGenes.contains(partialAmpGene + " (partial)"));
        assertTrue(amplifiedGenes.contains(fullAmpGene));
    }

    @Test
    public void shouldReturnKeyDeletedGenes() {
        Set<CopyNumber> copyNumbers = Set.of(copyNumber(CopyNumberType.FULL_GAIN, "full amp", DriverLikelihood.HIGH, true),
                copyNumber(CopyNumberType.PARTIAL_GAIN, "partial amp", DriverLikelihood.HIGH, true),
                copyNumber(CopyNumberType.LOSS, EXPECTED_GENE, DriverLikelihood.HIGH, true),
                copyNumber(CopyNumberType.LOSS, "low", DriverLikelihood.LOW, true),
                copyNumber(CopyNumberType.LOSS, "non-reportable", DriverLikelihood.HIGH, false));

        MolecularDrivers molecularDrivers = ImmutableMolecularDrivers.builder().addAllCopyNumbers(copyNumbers).build();
        assertExpectedStreamResult(summarizer(molecularDrivers).keyDeletedGenes());
    }

    @Test
    public void shouldReturnKeyHomozygouslyDisruptedGenes() {
        Set<HomozygousDisruption> homozygousDisruptions = Set.of(homozygousDisruption(EXPECTED_GENE, DriverLikelihood.HIGH, true),
                homozygousDisruption("non-reportable", DriverLikelihood.HIGH, false),
                homozygousDisruption("medium likelihood", DriverLikelihood.MEDIUM, true));

        MolecularDrivers molecularDrivers = ImmutableMolecularDrivers.builder().addAllHomozygousDisruptions(homozygousDisruptions).build();
        assertExpectedStreamResult(summarizer(molecularDrivers).keyHomozygouslyDisruptedGenes());
    }

    @Test
    public void shouldReturnKeyFusions() {
        Set<Fusion> fusions = Set.of(fusion(EXPECTED_GENE, DriverLikelihood.HIGH, true),
                fusion("non-reportable", DriverLikelihood.HIGH, false),
                fusion("medium likelihood", DriverLikelihood.MEDIUM, true));

        MolecularDrivers molecularDrivers = ImmutableMolecularDrivers.builder().addAllFusions(fusions).build();
        assertExpectedStreamResult(summarizer(molecularDrivers).keyFusionEvents());
    }

    @Test
    public void shouldReturnKeyViruses() {
        Set<Virus> viruses = Set.of(virus("virus", DriverLikelihood.HIGH, true),
                virus("non-reportable", DriverLikelihood.HIGH, false),
                virus("medium likelihood", DriverLikelihood.MEDIUM, true));

        MolecularDrivers molecularDrivers = ImmutableMolecularDrivers.builder().addAllViruses(viruses).build();
        Set<String> keyViruses = summarizer(molecularDrivers).keyVirusEvents().collect(Collectors.toSet());
        assertEquals(1, keyViruses.size());
        assertTrue(keyViruses.contains(VirusType.MERKEL_CELL_VIRUS + " (" + VIRUS_INTEGRATIONS + " integrations detected)"));
    }

    @Test
    public void shouldReturnActionableEventsThatAreNotKeyDrivers() {
        ActionableEvidence externalEvidence = TestActionableEvidenceFactory.withExternalEligibleTrial("external");
        ActionableEvidence approvedTreatment = TestActionableEvidenceFactory.withApprovedTreatment("approved");
        List<EvaluatedCohort> cohorts = Collections.singletonList(EvaluatedCohortTestFactory.builder()
                .isPotentiallyEligible(true)
                .isOpen(true)
                .addMolecularEvents("expected medium likelihood variant",
                        "expected low likelihood virus",
                        "expected non-reportable virus",
                        "key virus",
                        "key gain",
                        "expected amplification",
                        "expected loss",
                        "expected key disruption",
                        "expected non-reportable fusion")
                .build());

        Set<Variant> variants = Set.of(variant("key variant", DriverLikelihood.HIGH, true, externalEvidence),
                variant("expected non-reportable variant", DriverLikelihood.HIGH, false, approvedTreatment),
                variant("expected medium likelihood variant", DriverLikelihood.MEDIUM, true),
                variant("no evidence", DriverLikelihood.MEDIUM, true));

        Set<CopyNumber> copyNumbers = Set.of(copyNumber(CopyNumberType.FULL_GAIN, "key gain", DriverLikelihood.HIGH, true),
                copyNumber(CopyNumberType.PARTIAL_GAIN, "expected amplification", null, false),
                copyNumber(CopyNumberType.LOSS, "expected loss", DriverLikelihood.HIGH, false),
                copyNumber(CopyNumberType.FULL_GAIN, "no evidence", DriverLikelihood.LOW, true));

        Set<HomozygousDisruption> homozygousDisruptions =
                Set.of(homozygousDisruption("key HD", DriverLikelihood.HIGH, true, approvedTreatment),
                        homozygousDisruption("expected non-reportable HD", DriverLikelihood.HIGH, false, approvedTreatment),
                        homozygousDisruption("expected null likelihood HD", null, true, externalEvidence));

        Set<Disruption> disruptions = Set.of(disruption("expected key disruption", DriverLikelihood.HIGH, true),
                disruption("expected non-reportable disruption", DriverLikelihood.LOW, false, approvedTreatment),
                disruption("no evidence disruption", DriverLikelihood.MEDIUM, false));

        Set<Fusion> fusions = Set.of(fusion("key fusion", DriverLikelihood.HIGH, true, externalEvidence),
                fusion("expected non-reportable fusion", DriverLikelihood.HIGH, false),
                fusion("expected medium likelihood fusion", DriverLikelihood.MEDIUM, true, approvedTreatment));

        Set<Virus> viruses = Set.of(virus("expected low likelihood virus", DriverLikelihood.LOW, true),
                virus("expected non-reportable virus", DriverLikelihood.LOW, false),
                virus("key virus", DriverLikelihood.HIGH, true));

        MolecularDrivers molecularDrivers = ImmutableMolecularDrivers.builder()
                .addAllVariants(variants)
                .addAllCopyNumbers(copyNumbers)
                .addAllHomozygousDisruptions(homozygousDisruptions)
                .addAllDisruptions(disruptions)
                .addAllFusions(fusions)
                .addAllViruses(viruses)
                .build();

        MolecularDriversSummarizer summarizer =
                MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(molecularDrivers, cohorts);
        Set<String> otherActionableEvents = summarizer.actionableEventsThatAreNotKeyDrivers().collect(Collectors.toSet());
        assertEquals(12, otherActionableEvents.size());
        assertTrue(otherActionableEvents.stream().allMatch(event -> event.startsWith("expected")));
    }

    private static Variant variant(String name, DriverLikelihood driverLikelihood, boolean isReportable) {
        return variant(name, driverLikelihood, isReportable, TestActionableEvidenceFactory.createEmpty());
    }

    private static Variant variant(String name, DriverLikelihood driverLikelihood, boolean isReportable, ActionableEvidence evidence) {
        return TestVariantFactory.builder()
                .gene(name)
                .event(name)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .evidence(evidence)
                .build();
    }

    private static CopyNumber copyNumber(CopyNumberType type, String name, DriverLikelihood driverLikelihood, boolean isReportable) {
        return TestCopyNumberFactory.builder()
                .type(type)
                .gene(name)
                .event(name)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .build();
    }

    private static HomozygousDisruption homozygousDisruption(String name, DriverLikelihood driverLikelihood, boolean isReportable) {
        return homozygousDisruption(name, driverLikelihood, isReportable, TestActionableEvidenceFactory.createEmpty());
    }

    private static HomozygousDisruption homozygousDisruption(String name, DriverLikelihood driverLikelihood, boolean isReportable,
            ActionableEvidence evidence) {
        return TestHomozygousDisruptionFactory.builder()
                .gene(name)
                .event(name)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .evidence(evidence)
                .build();
    }

    private static Disruption disruption(String name, DriverLikelihood driverLikelihood, boolean isReportable) {
        return disruption(name, driverLikelihood, isReportable, TestActionableEvidenceFactory.createEmpty());
    }

    private static Disruption disruption(String name, DriverLikelihood driverLikelihood, boolean isReportable,
            ActionableEvidence evidence) {
        return TestDisruptionFactory.builder()
                .gene(name)
                .event(name)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .evidence(evidence)
                .build();
    }

    private static Fusion fusion(String event, DriverLikelihood driverLikelihood, boolean isReportable) {
        return fusion(event, driverLikelihood, isReportable, TestActionableEvidenceFactory.createEmpty());
    }

    private static Fusion fusion(String event, DriverLikelihood driverLikelihood, boolean isReportable, ActionableEvidence evidence) {
        return TestFusionFactory.builder()
                .event(event)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .evidence(evidence)
                .build();
    }

    private static Virus virus(String event, DriverLikelihood driverLikelihood, boolean isReportable) {
        return TestVirusFactory.builder()
                .event(event)
                .driverLikelihood(driverLikelihood)
                .isReportable(isReportable)
                .type(VirusType.MERKEL_CELL_VIRUS)
                .integrations(VIRUS_INTEGRATIONS)
                .build();
    }

    private static MolecularDriversSummarizer summarizer(MolecularDrivers molecularDrivers) {
        return MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(molecularDrivers, Collections.emptyList());
    }

    private static void assertExpectedStreamResult(Stream<String> keyEntryStream) {
        Set<String> keyEntries = keyEntryStream.collect(Collectors.toSet());
        assertEquals(1, keyEntries.size());
        assertTrue(keyEntries.contains(EXPECTED_GENE));
    }
}