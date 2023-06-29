package com.hartwig.actin.treatment.ctc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry;
import com.hartwig.actin.treatment.ctc.config.TestCTCDatabaseEntryFactory;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.TestCohortDefinitionConfigFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class CohortStatusInterpreterTest {

    private static final int PARENT_OPEN_WITH_SLOTS_COHORT_ID = 1;
    private static final int CHILD_OPEN_WITH_SLOTS_COHORT_ID = 2;
    private static final int CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID = 3;
    private static final int CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID = 4;
    private static final int ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID = 5;

    private final List<CTCDatabaseEntry> entries = createTestEntries();

    @Test
    public void shouldIgnoreCohortsThatAreConfiguredAsNotAvailable() {
        CohortDefinitionConfig notAvailable = createWithCTCCohortIDs(CohortStatusInterpreter.NOT_AVAILABLE);
        assertNull(CohortStatusInterpreter.interpret(entries, notAvailable));
    }

    @Test
    public void shouldAssumeUnexplainedMissingCohortsAreClosed() {
        CohortDefinitionConfig unexplainedMissing = createWithCTCCohortIDs(CohortStatusInterpreter.NOT_IN_CTC_OVERVIEW_UNKNOWN_WHY);

        InterpretedCohortStatus status = CohortStatusInterpreter.interpret(entries, unexplainedMissing);
        assertFalse(status.open());
        assertFalse(status.slotsAvailable());
    }

    @Test
    public void shouldAssumeUnmappedClosedCohortsAreClosed() {
        CohortDefinitionConfig notMappedClosed = createWithCTCCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_CLOSED);

        InterpretedCohortStatus status = CohortStatusInterpreter.interpret(entries, notMappedClosed);
        assertFalse(status.open());
        assertFalse(status.slotsAvailable());
    }

    @Test
    public void shouldAssumeUnmappedUnavailableCohortsAreClosed() {
        CohortDefinitionConfig notMappedNotAvailable = createWithCTCCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE);

        InterpretedCohortStatus status = CohortStatusInterpreter.interpret(entries, notMappedNotAvailable);
        assertFalse(status.open());
        assertFalse(status.slotsAvailable());
    }

    @Test
    public void shouldAssumeCohortIsClosedForInvalidCohortConfig() {
        CohortDefinitionConfig idDoesNotExist = createWithCTCCohortIDs("12");

        InterpretedCohortStatus status = CohortStatusInterpreter.interpret(entries, idDoesNotExist);
        assertFalse(status.open());
        assertFalse(status.slotsAvailable());
    }

    @Test
    public void shouldBeAbleToDetermineStatusForSingleParent() {
        CohortDefinitionConfig config = createWithCTCCohortIDs(String.valueOf(PARENT_OPEN_WITH_SLOTS_COHORT_ID));

        InterpretedCohortStatus status = CohortStatusInterpreter.interpret(entries, config);
        assertTrue(status.open());
        assertTrue(status.slotsAvailable());
    }

    @Test
    public void shouldBeAbleToDetermineStatusForSingleChildConsistentWithParent() {
        CohortDefinitionConfig config = createWithCTCCohortIDs(String.valueOf(CHILD_OPEN_WITH_SLOTS_COHORT_ID));

        InterpretedCohortStatus status = CohortStatusInterpreter.interpret(entries, config);
        assertTrue(status.open());
        assertTrue(status.slotsAvailable());
    }

    @Test
    public void shouldStickToChildWhenInconsistentWithParent() {
        CohortDefinitionConfig config = createWithCTCCohortIDs(String.valueOf(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID));

        InterpretedCohortStatus status = CohortStatusInterpreter.interpret(entries, config);
        assertTrue(status.open());
        assertFalse(status.slotsAvailable());
    }

    @Test
    public void shouldBeAbleToDetermineStatusForMultipleChildrenConsistentWithParent() {
        CohortDefinitionConfig config = createWithCTCCohortIDs(String.valueOf(CHILD_OPEN_WITH_SLOTS_COHORT_ID),
                String.valueOf(ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID));

        InterpretedCohortStatus status = CohortStatusInterpreter.interpret(entries, config);
        assertTrue(status.open());
        assertTrue(status.slotsAvailable());
    }

    @Test
    public void shouldPickBestChildWhenBestChildIsInconsistentWithParent() {
        CohortDefinitionConfig config = createWithCTCCohortIDs(String.valueOf(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID),
                String.valueOf(CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID));

        InterpretedCohortStatus status = CohortStatusInterpreter.interpret(entries, config);
        assertTrue(status.open());
        assertFalse(status.slotsAvailable());
    }

    @Test
    public void noMatchIsConsideredInvalid() {
        assertFalse(CohortStatusInterpreter.hasValidCTCDatabaseMatches(Collections.emptyList()));
    }

    @Test
    public void nullMatchIsConsideredInvalid() {
        List<CTCDatabaseEntry> nullEntry = new ArrayList<>();
        nullEntry.add(null);

        assertFalse(CohortStatusInterpreter.hasValidCTCDatabaseMatches(nullEntry));
    }

    @Test
    public void entriesWithBothParentsAndChildAreConsideredInvalid() {
        assertFalse(CohortStatusInterpreter.hasValidCTCDatabaseMatches(entries));
    }

    @Test
    public void singleEntryIsAlwaysConsideredValid() {
        for (CTCDatabaseEntry entry : entries) {
            assertTrue(CohortStatusInterpreter.hasValidCTCDatabaseMatches(List.of(entry)));
        }
    }

    @NotNull
    private static List<CTCDatabaseEntry> createTestEntries() {
        CTCDatabaseEntry parentOpenWithSlots = createEntry(PARENT_OPEN_WITH_SLOTS_COHORT_ID, null, "Open", 1);
        CTCDatabaseEntry childOpenWithSlots = createEntry(CHILD_OPEN_WITH_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Open", 1);
        CTCDatabaseEntry childOpenWithoutSlots =
                createEntry(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Open", 0);
        CTCDatabaseEntry childClosedWithoutSlots =
                createEntry(CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Gesloten", 0);
        CTCDatabaseEntry anotherChildOpenWithSlots =
                createEntry(ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Open", 1);

        return List.of(parentOpenWithSlots, childOpenWithSlots, childOpenWithoutSlots, childClosedWithoutSlots, anotherChildOpenWithSlots);
    }

    @NotNull
    private static CTCDatabaseEntry createEntry(@Nullable Integer cohortId, @Nullable Integer cohortParentId, @Nullable String cohortStatus,
            @Nullable Integer cohortSlotsNumberAvailable) {
        return TestCTCDatabaseEntryFactory.builder()
                .cohortId(cohortId)
                .cohortParentId(cohortParentId)
                .cohortStatus(cohortStatus)
                .cohortSlotsNumberAvailable(cohortSlotsNumberAvailable)
                .build();
    }

    @NotNull
    private static CohortDefinitionConfig createWithCTCCohortIDs(@NotNull String... ctcCohortIDs) {
        return TestCohortDefinitionConfigFactory.builder().ctcCohortIds(Set.of(ctcCohortIDs)).build();
    }
}