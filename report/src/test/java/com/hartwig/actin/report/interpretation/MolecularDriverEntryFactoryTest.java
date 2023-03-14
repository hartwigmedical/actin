package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VirusType;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularDriverEntryFactoryTest {

    @Test
    public void canCreateMolecularDriverEntries() {
        MolecularRecord record = TestMolecularFactory.createExhaustiveTestMolecularRecord();

        MolecularDriverEntryFactory factory = MolecularDriverEntryFactory.fromEvaluatedCohorts(Collections.emptyList());
        Stream<MolecularDriverEntry> entries = factory.create(record);

        assertEquals(7, entries.count());
    }

    @Test
    public void shouldIncludeNonActionableReportableDrivers() {
        MolecularRecord record = createTestMolecularRecordWithDriverEvidence(TestActionableEvidenceFactory.createEmpty(), true);

        MolecularDriverEntryFactory factory = MolecularDriverEntryFactory.fromEvaluatedCohorts(Collections.emptyList());

        assertEquals(1, factory.create(record).count());
    }

    @Test
    public void shouldSkipNonActionableNotReportableDrivers() {
        MolecularRecord record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.createEmpty());

        MolecularDriverEntryFactory factory = MolecularDriverEntryFactory.fromEvaluatedCohorts(Collections.emptyList());

        assertEquals(0, factory.create(record).count());
    }

    @Test
    public void shouldIncludeNonReportableDriversWithActinTrialMatches() {
        MolecularRecord record = createTestMolecularRecordWithNonReportableDriverWithEvidence(
                TestActionableEvidenceFactory.createEmpty());
        String driverToFind = record.drivers().viruses().iterator().next().event();

        assertEquals(1, createFactoryWithCohortsForEvent(driverToFind).create(record).count());
    }

    @Test
    public void shouldIncludeNonReportableDriversWithApprovedTreatmentMatches() {
        MolecularRecord record = createTestMolecularRecordWithNonReportableDriverWithEvidence(
                TestActionableEvidenceFactory.withApprovedTreatment("treatment"));

        MolecularDriverEntryFactory factory = MolecularDriverEntryFactory.fromEvaluatedCohorts(Collections.emptyList());

        assertEquals(1, factory.create(record).count());
    }

    @Test
    public void shouldIncludeNonReportableDriversWithExternalTrialMatches() {
        MolecularRecord record = createTestMolecularRecordWithNonReportableDriverWithEvidence(
                TestActionableEvidenceFactory.withExternalEligibleTrial("trial 1"));

        MolecularDriverEntryFactory factory = MolecularDriverEntryFactory.fromEvaluatedCohorts(Collections.emptyList());

        assertEquals(1, factory.create(record).count());
    }

    @Test
    public void canMatchActinTrialToMolecularDrivers() {
        MolecularRecord record = TestMolecularFactory.createProperTestMolecularRecord();
        assertFalse(record.drivers().variants().isEmpty());

        Variant firstVariant = record.drivers().variants().iterator().next();

        String driverToFind = firstVariant.event();

        MolecularDriverEntry entry = createFactoryWithCohortsForEvent(driverToFind).create(record)
                .filter(molecularDriverEntry -> molecularDriverEntry.driver().startsWith(driverToFind))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Could not find molecular driver entry starting with driver: " + driverToFind));
        assertEquals(1, entry.actinTrials().size());
        assertTrue(entry.actinTrials().contains("trial 1"));
    }

    @NotNull
    private static MolecularRecord createTestMolecularRecordWithNonReportableDriverWithEvidence(ActionableEvidence evidence) {
        return createTestMolecularRecordWithDriverEvidence(evidence, false);
    }

    @NotNull
    public static MolecularRecord createTestMolecularRecordWithDriverEvidence(ActionableEvidence evidence, boolean isReportable) {
        return ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .drivers(createDriversWithEvidence(evidence, isReportable))
                .build();
    }

    @NotNull
    private static MolecularDrivers createDriversWithEvidence(ActionableEvidence evidence, boolean isReportable) {
        return ImmutableMolecularDrivers.builder()
                .addViruses(TestVirusFactory.builder()
                        .isReportable(isReportable)
                        .event("HPV positive")
                        .evidence(evidence)
                        .name("Human papillomavirus type 16")
                        .type(VirusType.HUMAN_PAPILLOMA_VIRUS)
                        .integrations(3)
                        .isReliable(true)
                        .build())
                .build();
    }

    private MolecularDriverEntryFactory createFactoryWithCohortsForEvent(String event) {
        EvaluatedCohort openCohortForVariant = EvaluatedCohortTestFactory.builder()
                .acronym("trial 1")
                .addMolecularEvents(event)
                .isPotentiallyEligible(true)
                .isOpen(true)
                .build();

        EvaluatedCohort closedCohortForVariant = EvaluatedCohortTestFactory.builder()
                .acronym("trial 2")
                .addMolecularEvents(event)
                .isPotentiallyEligible(true)
                .isOpen(false)
                .build();

        return MolecularDriverEntryFactory.fromEvaluatedCohorts(Lists.newArrayList(openCohortForVariant, closedCohortForVariant));
    }
}