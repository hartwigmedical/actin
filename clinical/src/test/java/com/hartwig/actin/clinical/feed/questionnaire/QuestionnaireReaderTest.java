package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class QuestionnaireReaderTest {

    @Test
    public void canReadQuestionnaire() {

        assertEquals(4, QuestionnaireReader.read("this \n is \n a \n questionnaire").length);

        String[] lines = QuestionnaireReader.read(QuestionnaireReader.TERMS_TO_CLEAN.iterator().next() + "\n test");
        assertEquals(2, lines.length);
        assertEquals(Strings.EMPTY, lines[0]);

        lines = QuestionnaireReader.read("value1: x\nand y\nvalue2: z\n\nheader\nvalue3: 5\nvalue4: 6\n7");
        assertEquals(6, lines.length);
        assertEquals("value4: 6,7", lines[5]);
    }
}