package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireFileTest {

    private static final String TEST_QUESTIONNAIRE_TSV = Resources.getResource("clinical/questionnaire.tsv").getPath();

    @Test
    public void canReadTestFile() throws IOException {
        List<QuestionnaireEntry> entries = QuestionnaireFile.read(TEST_QUESTIONNAIRE_TSV);
        assertEquals(2, entries.size());

        QuestionnaireEntry entry1 = findByParentIdentifierValue(entries, "XX");
        assertEquals("ACTN-01-02-9999", entry1.subject());

        QuestionnaireEntry entry2 = findByParentIdentifierValue(entries, "YY");
        assertEquals("ACTN-01-02-9999", entry2.subject());
    }

    @NotNull
    private static QuestionnaireEntry findByParentIdentifierValue(@NotNull List<QuestionnaireEntry> entries,
            @NotNull String parentIdentifierValue) {
        for (QuestionnaireEntry entry : entries) {
            if (entry.parentIdentifierValue().equals(parentIdentifierValue)) {
                return entry;
            }
        }

        throw new IllegalStateException("No questionnaire entry found with parentIdentifierValue '" + parentIdentifierValue + "'");
    }
}