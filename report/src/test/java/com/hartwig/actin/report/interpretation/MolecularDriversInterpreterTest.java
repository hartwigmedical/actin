package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;

import org.junit.Test;

public class MolecularDriversInterpreterTest {

    public static final String EVENT_VARIANT = "variant";
    public static final String EVENT_CN = "CN";
    public static final String EVENT_HD = "HD";
    public static final String EVENT_DISRUPTION = "disruption";
    public static final String EVENT_FUSION = "fusion";
    public static final String EVENT_VIRUS = "virus";

    @Test
    public void shouldIncludeNonActionableReportableDrivers() {
        MolecularRecord record = createTestMolecularRecordWithDriverEvidence(TestActionableEvidenceFactory.createEmpty(), true);
        assertCountForRecord(1, record);
    }

    @Test
    public void shouldSkipNonActionableNotReportableDrivers() {
        MolecularRecord record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.createEmpty());
        assertCountForRecord(0, record);
    }

    @Test
    public void shouldIncludeNonReportableDriversWithActinTrialMatches() {
        MolecularRecord record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.createEmpty());
        assertCountForRecordAndCohorts(1,
                record,
                createCohortsForEvents(List.of(EVENT_VARIANT, EVENT_CN, EVENT_HD, EVENT_DISRUPTION, EVENT_FUSION, EVENT_VIRUS)));
    }

    @Test
    public void shouldIncludeNonReportableDriversWithApprovedTreatmentMatches() {
        MolecularRecord record =
                createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.withApprovedTreatment("treatment"));
        assertCountForRecord(1, record);
    }

    @Test
    public void shouldIncludeNonReportableDriversWithExternalTrialMatches() {
        MolecularRecord record =
                createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.withExternalEligibleTrial(
                        "trial 1"));

        assertCountForRecord(1, record);
    }

    private static void assertCountForRecord(int expectedCount, MolecularRecord molecularRecord) {
        assertCountForRecordAndCohorts(expectedCount, molecularRecord, Collections.emptyList());
    }

    private static void assertCountForRecordAndCohorts(int expectedCount, MolecularRecord molecularRecord, List<EvaluatedCohort> cohorts) {
        MolecularDriversInterpreter interpreter =
                new MolecularDriversInterpreter(molecularRecord.drivers(), new EvaluatedCohortsInterpreter(cohorts));
        assertEquals(expectedCount, interpreter.filteredVariants().count());
        assertEquals(expectedCount, interpreter.filteredCopyNumbers().count());
        assertEquals(expectedCount, interpreter.filteredHomozygousDisruptions().count());
        assertEquals(expectedCount, interpreter.filteredDisruptions().count());
        assertEquals(expectedCount, interpreter.filteredFusions().count());
        assertEquals(expectedCount, interpreter.filteredViruses().count());
    }

    private static MolecularRecord createTestMolecularRecordWithNonReportableDriverWithEvidence(ActionableEvidence evidence) {
        return createTestMolecularRecordWithDriverEvidence(evidence, false);
    }

    private static MolecularRecord createTestMolecularRecordWithDriverEvidence(ActionableEvidence evidence, boolean isReportable) {
        return ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .drivers(createDriversWithEvidence(evidence, isReportable))
                .build();
    }

    private static MolecularDrivers createDriversWithEvidence(ActionableEvidence evidence, boolean isReportable) {
        return ImmutableMolecularDrivers.builder()
                .addVariants(TestVariantFactory.builder().isReportable(isReportable).evidence(evidence).event(EVENT_VARIANT).build())
                .addCopyNumbers(TestCopyNumberFactory.builder().isReportable(isReportable).evidence(evidence).event(EVENT_CN).build())
                .addHomozygousDisruptions(TestHomozygousDisruptionFactory.builder()
                        .isReportable(isReportable)
                        .evidence(evidence)
                        .event(EVENT_HD)
                        .build())
                .addDisruptions(TestDisruptionFactory.builder()
                        .isReportable(isReportable)
                        .evidence(evidence)
                        .event(EVENT_DISRUPTION)
                        .build())
                .addFusions(TestFusionFactory.builder().isReportable(isReportable).evidence(evidence).event(EVENT_FUSION).build())
                .addViruses(TestVirusFactory.builder().isReportable(isReportable).evidence(evidence).event(EVENT_VIRUS).build())
                .build();
    }

    private static List<EvaluatedCohort> createCohortsForEvents(List<String> events) {
        EvaluatedCohort openCohortForVariant = EvaluatedCohortTestFactory.builder()
                .acronym("trial 1")
                .addAllMolecularEvents(events)
                .isPotentiallyEligible(true)
                .isOpen(true)
                .build();

        EvaluatedCohort closedCohortForVariant = EvaluatedCohortTestFactory.builder()
                .acronym("trial 2")
                .addAllMolecularEvents(events)
                .isPotentiallyEligible(true)
                .isOpen(false)
                .build();

        return List.of(openCohortForVariant, closedCohortForVariant);
    }
}