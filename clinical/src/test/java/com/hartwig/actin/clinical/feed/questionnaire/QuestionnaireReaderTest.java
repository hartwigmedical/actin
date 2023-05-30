package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireReaderTest {

    @Test
    public void canReadQuestionnaire() {
        QuestionnaireEntry entry1 = entry("this \n is \n a \n questionnaire");

        assertEquals(4, QuestionnaireReader.read(entry1).length);

        QuestionnaireEntry entry2 = entry(QuestionnaireReader.TERMS_TO_CLEAN.iterator().next() + "\n test");

        String[] lines = QuestionnaireReader.read(entry2);
        assertEquals(2, lines.length);
        assertEquals(Strings.EMPTY, lines[0]);

        QuestionnaireEntry entry3 = entry("value1: x\nand y\nvalue2: z\n\nheader\nvalue3: 5\nvalue4: 6\n7");

        lines = QuestionnaireReader.read(entry3);
        assertEquals(6, lines.length);
        assertEquals("value4: 6,7", lines[5]);
    }

    @NotNull
    private static QuestionnaireEntry entry(@NotNull String questionnaire) {
        return ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .text(questionnaire)
                .build();
    }
}