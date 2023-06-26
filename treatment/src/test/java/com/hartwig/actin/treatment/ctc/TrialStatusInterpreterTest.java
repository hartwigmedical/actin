package com.hartwig.actin.treatment.ctc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry;
import com.hartwig.actin.treatment.ctc.config.TestCTCDatabaseEntryFactory;
import com.hartwig.actin.treatment.trial.config.TestTrialDefinitionConfigFactory;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TrialStatusInterpreterTest {

    private static final String STUDY_METC_1 = "1";
    private static final String STUDY_METC_2 = "2";

    @Test
    public void shouldReturnNullForEmptyCTCDatabase() {
        assertNull(TrialStatusInterpreter.isOpen(List.of(), createConfig("trial 1")));
    }

    @Test
    public void shouldResolveToOpenForTrialsWithExclusivelyOpenEntries() {
        CTCDatabaseEntry entry1 = createEntry(STUDY_METC_2, "Gesloten");
        CTCDatabaseEntry entry2 = createEntry(STUDY_METC_1, "Open");
        TrialDefinitionConfig config = createConfig(TrialStatusInterpreter.extractTrialId(entry2));

        assertEquals(true, TrialStatusInterpreter.isOpen(List.of(entry1, entry2), config));
    }

    @Test
    public void shouldResolveToClosedForTrialsWithInconsistentEntries() {
        CTCDatabaseEntry entry1 = createEntry(STUDY_METC_1, "Gesloten");
        CTCDatabaseEntry entry2 = createEntry(STUDY_METC_1, "Open");
        TrialDefinitionConfig config = createConfig(TrialStatusInterpreter.extractTrialId(entry1));

        assertEquals(false, TrialStatusInterpreter.isOpen(List.of(entry1, entry2), config));
    }

    @Test
    public void shouldResolveToClosedForTrialsWithClosedEntriesExclusively() {
        CTCDatabaseEntry entry1 = createEntry(STUDY_METC_1, "Gesloten");
        CTCDatabaseEntry entry2 = createEntry(STUDY_METC_2, "Open");
        TrialDefinitionConfig config = createConfig(TrialStatusInterpreter.extractTrialId(entry1));

        assertEquals(false, TrialStatusInterpreter.isOpen(List.of(entry1, entry2), config));
    }

    @NotNull
    private static CTCDatabaseEntry createEntry(@NotNull String studyMETC, @NotNull String studyStatus) {
        return TestCTCDatabaseEntryFactory.builder().studyMETC(studyMETC).studyStatus(studyStatus).build();
    }

    @NotNull
    private static TrialDefinitionConfig createConfig(@NotNull String trialId) {
        return TestTrialDefinitionConfigFactory.builder().trialId(trialId).build();
    }
}