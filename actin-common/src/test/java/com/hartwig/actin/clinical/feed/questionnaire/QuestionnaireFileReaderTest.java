package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.io.Resources;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireFileReaderTest {

    private static final String TEST_QUESTIONNAIRE_TSV = Resources.getResource("clinical/feed/questionnaire.tsv").getPath();

    @Test
    public void canReadTestFile() throws IOException {
        List<QuestionnaireEntry> entries = new QuestionnaireFileReader().read(TEST_QUESTIONNAIRE_TSV);
        assertEquals(2, entries.size());

        QuestionnaireEntry entry1 = findByParentIdentifierValue(entries, "XX");
        assertEquals("ACTN-01-02-9999", entry1.subject());
        assertEquals(LocalDate.of(2020, 8, 28), entry1.authoredDateTime());
        assertEquals("A", entry1.questionnaireQuestionnaireValue());
        assertEquals("INT Consult", entry1.description());
        assertEquals("Beloop", entry1.itemText());

        assertEquals(30, entry1.itemAnswerValueValueString().split("\n").length);
        assertTrue(entry1.itemAnswerValueValueString().startsWith("ACTIN Questionnaire"));
        assertTrue(entry1.itemAnswerValueValueString().contains("CNS lesions yes/no/unknown"));
        assertTrue(entry1.itemAnswerValueValueString().contains("Other (e.g. pleural effusion)"));

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