package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class QuestionnaireCurationTest {

    @Test
    public void canCurateStage() {
        assertEquals(TumorStage.IIB, QuestionnaireCuration.toStage("IIb"));
        assertEquals(TumorStage.II, QuestionnaireCuration.toStage("2"));
        assertEquals(TumorStage.III, QuestionnaireCuration.toStage("3"));
        assertEquals(TumorStage.IV, QuestionnaireCuration.toStage("4"));

        assertNull(QuestionnaireCuration.toStage(null));
        assertNull(QuestionnaireCuration.toStage(Strings.EMPTY));
        assertNull(QuestionnaireCuration.toStage("not a stage"));
    }

    @Test
    public void canCurateOption() {
        assertTrue(QuestionnaireCuration.toOption("YES"));
        assertFalse(QuestionnaireCuration.toOption("no"));

        assertNull(QuestionnaireCuration.toOption(null));
        assertNull(QuestionnaireCuration.toOption(Strings.EMPTY));
        assertNull(QuestionnaireCuration.toOption("-"));
        assertNull(QuestionnaireCuration.toOption("nvt"));
        assertNull(QuestionnaireCuration.toOption("not an option"));
    }

    @Test
    public void canCurateWHO() {
        assertEquals(1, (int) QuestionnaireCuration.toWHO("1"));

        assertNull(QuestionnaireCuration.toWHO(null));
        assertNull(QuestionnaireCuration.toWHO(Strings.EMPTY));
        assertNull(QuestionnaireCuration.toWHO("-1"));
        assertNull(QuestionnaireCuration.toWHO("5"));
    }
}