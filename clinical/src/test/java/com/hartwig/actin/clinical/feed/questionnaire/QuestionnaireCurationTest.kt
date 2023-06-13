package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class QuestionnaireCurationTest {

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
    public void canCurateWHO() {
        assertEquals(1, (int) QuestionnaireCuration.toWHO("1"));

        assertNull(QuestionnaireCuration.toWHO(null));
        assertNull(QuestionnaireCuration.toWHO(Strings.EMPTY));
        assertNull(QuestionnaireCuration.toWHO("-1"));
        assertNull(QuestionnaireCuration.toWHO("12"));
    }

    @Test
    public void shouldExtractSecondaryPrimaryAndLastTreatmentDateWhenAvailable() {
        assertEquals(Collections.singletonList("sarcoma | Feb 2020"), QuestionnaireCuration.toSecondaryPrimaries("sarcoma", "Feb 2020"));
    }

    @Test
    public void shouldExtractSecondaryPrimaryWhenLastTreatmentDateNotAvailable() {
        assertEquals(Collections.singletonList("sarcoma"), QuestionnaireCuration.toSecondaryPrimaries("sarcoma", ""));
    }

    @Test
    public void shouldReturnNullForEmptyInfectionStatus() {
        InfectionStatus infectionStatus = QuestionnaireCuration.toInfectionStatus("");
        assertNull(infectionStatus);
    }

    @Test
    public void shouldReturnNullForUnknownInfectionStatus() {
        InfectionStatus infectionStatus = QuestionnaireCuration.toInfectionStatus("unknown");
        assertNull(infectionStatus);
    }

    @Test
    public void shouldExtractPositiveInfectionStatus() {
        String infectionDescription = "yes";
        InfectionStatus infectionStatus = QuestionnaireCuration.toInfectionStatus(infectionDescription);
        assertNotNull(infectionStatus);
        assertTrue(infectionStatus.hasActiveInfection());
        assertEquals(infectionDescription, infectionStatus.description());
    }

    @Test
    public void shouldExtractNegativeInfectionStatus() {
        String infectionDescription = "no";
        InfectionStatus infectionStatus = QuestionnaireCuration.toInfectionStatus(infectionDescription);
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());
        assertEquals(infectionDescription, infectionStatus.description());
    }

    @Test
    public void shouldExtractInfectionDescriptionAndSetActive() {
        String infectionDescription = "infection";
        InfectionStatus infectionStatus = QuestionnaireCuration.toInfectionStatus(infectionDescription);
        assertNotNull(infectionStatus);
        assertTrue(infectionStatus.hasActiveInfection());
        assertEquals(infectionDescription, infectionStatus.description());
    }

    @Test
    public void shouldReturnNullForEmptyECG() {
        ECG ECGStatus = QuestionnaireCuration.toECG("");
        assertNull(ECGStatus);
    }

    @Test
    public void shouldReturnNullForUnknownECG() {
        ECG ECGStatus = QuestionnaireCuration.toECG("unknown");
        assertNull(ECGStatus);
    }

    @Test
    public void shouldExtractPositiveECG() {
        String ECGDescription = "yes";
        ECG ECGStatus = QuestionnaireCuration.toECG(ECGDescription);
        assertNotNull(ECGStatus);
        assertTrue(ECGStatus.hasSigAberrationLatestECG());
        assertEquals(ECGDescription, ECGStatus.aberrationDescription());
    }

    @Test
    public void shouldExtractNegativeECG() {
        String ECGDescription = "no";
        ECG ECGStatus = QuestionnaireCuration.toECG(ECGDescription);
        assertNotNull(ECGStatus);
        assertFalse(ECGStatus.hasSigAberrationLatestECG());
        assertEquals(ECGDescription, ECGStatus.aberrationDescription());
    }

    @Test
    public void shouldExtractECGDescriptionAndIndicatePresence() {
        String ECGDescription = "ECG";
        ECG ECGStatus = QuestionnaireCuration.toECG(ECGDescription);
        assertNotNull(ECGStatus);
        assertTrue(ECGStatus.hasSigAberrationLatestECG());
        assertEquals(ECGDescription, ECGStatus.aberrationDescription());
    }
}