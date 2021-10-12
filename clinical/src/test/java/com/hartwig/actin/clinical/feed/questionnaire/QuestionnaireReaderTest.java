package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class QuestionnaireReaderTest {

    @Test
    public void canReadQuestionnaire() {
        QuestionnaireEntry entry1 = ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString("this \n is \n a \n questionnaire")
                .build();

        assertEquals(4, QuestionnaireReader.read(entry1).length);

        QuestionnaireEntry entry2 = ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(QuestionnaireReader.TERMS_TO_CLEAN.iterator().next() + "\n test")
                .build();

        String[] lines = QuestionnaireReader.read(entry2);
        assertEquals(2, lines.length);
        assertEquals(Strings.EMPTY, lines[0]);

        QuestionnaireEntry entry3 = ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString("value1: x\nand y\nvalue2: z\n\nheader\nvalue3: 5\nvalue4: 6\n7")
                .build();

        lines = QuestionnaireReader.read(entry3);
        assertEquals(6, lines.length);
        assertEquals("value4: 6, 7", lines[5]);
    }
}