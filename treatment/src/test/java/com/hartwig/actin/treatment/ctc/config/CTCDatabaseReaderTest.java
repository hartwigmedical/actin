package com.hartwig.actin.treatment.ctc.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.google.common.io.Resources;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CTCDatabaseReaderTest {

    private static final String CTC_CONFIG_DIRECTORY = Resources.getResource("ctc_config").getPath();

    @Test
    public void shouldLoadExpectedDatabaseFromTestDirectory() throws IOException {
        CTCDatabase database = CTCDatabaseReader.read(CTC_CONFIG_DIRECTORY);

        assertEntries(database.entries());
        assertStudyMETCsToIgnore(database.studyMETCsToIgnore());
        assertUnmappedCohortIds(database.unmappedCohortIds());
    }

    private static void assertEntries(@NotNull List<CTCDatabaseEntry> entries) {
        assertEquals(2, entries.size());

        CTCDatabaseEntry entry1 = findEntryByStudyId(entries, 1);
        assertEquals("METC 1", entry1.studyMETC());
        assertEquals("StudyWithCohort", entry1.studyAcronym());
        assertEquals("This is a study with cohort", entry1.studyTitle());
        assertEquals("Open", entry1.studyStatus());
        assertEquals(1, (int) entry1.cohortId());
        assertEquals(2, (int) entry1.cohortParentId());
        assertEquals("Cohort A", entry1.cohortName());
        assertEquals("Closed", entry1.cohortStatus());
        assertEquals(5, (int) entry1.cohortSlotsNumberAvailable());
        assertEquals("23-04-04", entry1.cohortSlotsDateAvailable());

        CTCDatabaseEntry entry2 = findEntryByStudyId(entries, 2);
        assertEquals("METC 2", entry2.studyMETC());
        assertEquals("StudyWithoutCohort", entry2.studyAcronym());
        assertEquals("This is a study without cohort", entry2.studyTitle());
        assertEquals("Closed", entry2.studyStatus());
        assertNull(entry2.cohortId());
        assertNull(entry2.cohortParentId());
        assertNull(entry2.cohortName());
        assertNull(entry2.cohortStatus());
        assertNull(entry2.cohortSlotsNumberAvailable());
        assertNull(entry2.cohortSlotsDateAvailable());
    }

    @NotNull
    private static CTCDatabaseEntry findEntryByStudyId(@NotNull List<CTCDatabaseEntry> entries, int studyIdToFind) {
        return entries.stream().filter(entry -> entry.studyId() == studyIdToFind).findFirst().orElseThrow();
    }

    private static void assertStudyMETCsToIgnore(@NotNull Set<String> studyMETCsToIgnore) {
        assertEquals(1, studyMETCsToIgnore.size());

        assertTrue(studyMETCsToIgnore.contains("METC 1"));
    }

    private static void assertUnmappedCohortIds(@NotNull Set<Integer> unmappedCohortIds) {
        assertEquals(1, unmappedCohortIds.size());

        assertTrue(unmappedCohortIds.contains(1));
    }
}