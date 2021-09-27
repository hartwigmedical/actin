package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class QuestionnaireUtilTest {

    @Test
    public void canParseStage() {
        assertEquals(TumorStage.IIB, QuestionnaireUtil.parseStage("IIb"));
        assertEquals(TumorStage.II, QuestionnaireUtil.parseStage("2"));
        assertEquals(TumorStage.III, QuestionnaireUtil.parseStage("3"));
        assertEquals(TumorStage.IV, QuestionnaireUtil.parseStage("4"));

        assertNull(QuestionnaireUtil.parseStage(null));
        assertNull(QuestionnaireUtil.parseStage(Strings.EMPTY));
        assertNull(QuestionnaireUtil.parseStage("not a stage"));
    }

    @Test
    public void canParseOption() {
        assertTrue(QuestionnaireUtil.parseOption("YES"));
        assertFalse(QuestionnaireUtil.parseOption("no"));

        assertNull(QuestionnaireUtil.parseOption(null));
        assertNull(QuestionnaireUtil.parseOption(Strings.EMPTY));
        assertNull(QuestionnaireUtil.parseOption("-"));
        assertNull(QuestionnaireUtil.parseOption("nvt"));
        assertNull(QuestionnaireUtil.parseOption("not an option"));
    }
}