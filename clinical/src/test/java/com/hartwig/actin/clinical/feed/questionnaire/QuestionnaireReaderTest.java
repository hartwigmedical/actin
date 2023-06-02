package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class QuestionnaireReaderTest {

    @Test
    public void shouldReadSimpleQuestionnaire() {
        String questionnaireText = "this: \n is: \n a: \n questionnaire:";
        assertEquals(4, QuestionnaireReader.read(questionnaireText, Arrays.asList(questionnaireText.split(": \n "))).length);
    }

    @Test
    public void shouldCleanTermsFromQuestionnaire() {
        String[] lines = QuestionnaireReader.read(String.join("", QuestionnaireReader.TERMS_TO_CLEAN) + "\n test:", List.of("test"));
        assertEquals(2, lines.length);
        assertEquals(Strings.EMPTY, lines[0]);
    }

    @Test
    public void shouldMergeLinesForMultilineResponses() {
        String[] lines =
                QuestionnaireReader.read("value1: x\nand y\nand more\nvalue2: z\n\nheader\nvalue3: 5\nvalue4: 6\n7", List.of("value"));
        assertEquals(6, lines.length);
        assertEquals("value1: x,and y,and more", lines[0]);
        assertEquals("value4: 6,7", lines[5]);
    }
}