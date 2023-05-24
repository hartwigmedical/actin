package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class QuestionnaireRawEntryMapperTest {

    @Test
    public void shouldReplaceStringInQuestionnaireEntryUsingFileMapping() throws IOException {
        QuestionnaireRawEntryMapper questionnaireRawEntryMapper =
                QuestionnaireRawEntryMapper.createFromFile(Resources.getResource("feed/questionnaire_mapping.tsv").getPath());
        assertEquals("a much better entry", questionnaireRawEntryMapper.correctQuestionnaireEntry("a problematic, incorrect entry"));
    }
}