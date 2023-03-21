package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularDriverEntryFactoryTest {

    @Test
    public void canCreateMolecularDriverEntries() {
        MolecularRecord record = TestMolecularFactory.createExhaustiveTestMolecularRecord();

        MolecularDriverEntryFactory factory = createFactoryForMolecularRecord(record);
        Stream<MolecularDriverEntry> entries = factory.create();

        assertEquals(7, entries.count());
    }

    @Test
    public void shouldIncludeNonActionableReportableDrivers() {
        MolecularRecord record = createTestMolecularRecordWithDriverEvidence(TestActionableEvidenceFactory.createEmpty(), true);

        MolecularDriverEntryFactory factory = createFactoryForMolecularRecord(record);

        assertEquals(1, factory.create().count());
    }

    @Test
    public void shouldSkipNonActionableNotReportableDrivers() {
        MolecularRecord record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.createEmpty());

        MolecularDriverEntryFactory factory = createFactoryForMolecularRecord(record);

        assertEquals(0, factory.create().count());
    }

    @Test
    public void shouldIncludeNonReportableDriversWithActinTrialMatches() {
        MolecularRecord record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.createEmpty());
        String driverToFind = record.drivers().viruses().iterator().next().event();

        assertEquals(1, createFactoryWithCohortsForEvent(record, driverToFind).create().count());
    }

    @Test
    public void shouldIncludeNonReportableDriversWithApprovedTreatmentMatches() {
        MolecularRecord record =
                createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.withApprovedTreatment("treatment"));

        MolecularDriverEntryFactory factory = createFactoryForMolecularRecord(record);

        assertEquals(1, factory.create().count());
    }

    @Test
    public void shouldIncludeNonReportableDriversWithExternalTrialMatches() {
        MolecularRecord record =
                createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.withExternalEligibleTrial(
                        "trial 1"));

        MolecularDriverEntryFactory factory = createFactoryForMolecularRecord(record);

        assertEquals(1, factory.create().count());
    }

    @Test
    public void canMatchActinTrialToMolecularDrivers() {
        MolecularRecord record = TestMolecularFactory.createProperTestMolecularRecord();
        assertFalse(record.drivers().variants().isEmpty());

        Variant firstVariant = record.drivers().variants().iterator().next();

        String driverToFind = firstVariant.event();

        MolecularDriverEntry entry = createFactoryWithCohortsForEvent(record, driverToFind).create()
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
    private static MolecularRecord createTestMolecularRecordWithDriverEvidence(ActionableEvidence evidence, boolean isReportable) {
        return ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .drivers(createDriversWithEvidence(evidence, isReportable))
                .build();
    }

    @NotNull
    private static MolecularDrivers createDriversWithEvidence(ActionableEvidence evidence, boolean isReportable) {
        return ImmutableMolecularDrivers.builder()
                .addViruses(TestVirusFactory.builder().isReportable(isReportable).evidence(evidence).build())
                .build();
    }

    private static MolecularDriverEntryFactory createFactoryForMolecularRecord(MolecularRecord molecular) {
        return createFactoryForMolecularRecordAndCohorts(molecular, Collections.emptyList());
    }

    private static MolecularDriverEntryFactory createFactoryForMolecularRecordAndCohorts(MolecularRecord molecular,
            List<EvaluatedCohort> cohorts) {
        return new MolecularDriverEntryFactory(new MolecularDriversDetails(molecular.drivers(), new EvaluatedCohortsInterpreter(cohorts)));
    }

    private MolecularDriverEntryFactory createFactoryWithCohortsForEvent(MolecularRecord molecularRecord, String event) {
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

        return createFactoryForMolecularRecordAndCohorts(molecularRecord, List.of(openCohortForVariant, closedCohortForVariant));
    }
}