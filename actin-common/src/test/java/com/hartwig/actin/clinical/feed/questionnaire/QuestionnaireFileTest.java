package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class QuestionnaireFileTest {

    private static final String TEST_QUESTIONNAIRE_TSV = Resources.getResource("clinical/questionnaire.tsv").getPath();

    @Test
    public void canReadTestFile() throws IOException {
        assertEquals(2, QuestionnaireFile.read(TEST_QUESTIONNAIRE_TSV).size());
    }
}