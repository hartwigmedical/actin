package com.hartwig.actin.molecular.orange.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;

import com.google.common.io.Resources;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.chord.ChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterRecord;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeJsonTest {

    private static final String MINIMAL_ORANGE_JSON = Resources.getResource("orange/minimal.orange.json").getPath();
    private static final String EXHAUSTIVE_ORANGE_JSON = Resources.getResource("orange/exhaustive.orange.json").getPath();
    private static final String PROPER_ORANGE_JSON = Resources.getResource("orange/proper.orange.json").getPath();

    private static final double EPSILON = 1.0E-2;

    @Test
    public void canReadMinimalOrangeRecordJson() throws IOException {
        OrangeRecord record = OrangeJson.read(MINIMAL_ORANGE_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertNotNull(record.reportDate());
        assertNotNull(record.purple());
        assertNotNull(record.linx());
        assertNotNull(record.peach());
        assertNotNull(record.cuppa());
        assertNotNull(record.virusInterpreter());
        assertNotNull(record.chord());
        assertNotNull(record.protect());
    }

    @Test
    public void canReadExhaustiveOrangeRecordJson() throws IOException {
        OrangeRecord record = OrangeJson.read(EXHAUSTIVE_ORANGE_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertNotNull(record.reportDate());
        assertNotNull(record.purple());
        assertNotNull(record.linx());
        assertNotNull(record.peach());
        assertNotNull(record.cuppa());
        assertNotNull(record.virusInterpreter());
        assertNotNull(record.chord());
        assertNotNull(record.protect());
    }

    @Test
    public void canReadProperOrangeRecordJson() throws IOException {
        OrangeRecord record = OrangeJson.read(PROPER_ORANGE_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertEquals(LocalDate.of(2022, 1, 20), record.reportDate());

        assertPurple(record.purple());
        assertLinx(record.linx());
        assertPeach(record.peach());
        assertCuppa(record.cuppa());
        assertVirusInterpreter(record.virusInterpreter());
        assertChord(record.chord());
        assertProtect(record.protect());
    }

    private static void assertPurple(@NotNull PurpleRecord purple) {
        assertTrue(purple.hasReliableQuality());
        assertEquals("MSS", purple.microsatelliteStabilityStatus());
        assertEquals(13.71, purple.tumorMutationalBurden(), EPSILON);
        assertEquals(185, purple.tumorMutationalLoad());
    }

    private static void assertLinx(@NotNull LinxRecord linx) {
        assertNotNull(linx);
    }

    private static void assertPeach(@NotNull PeachRecord peach) {
        assertNotNull(peach);
    }

    private static void assertCuppa(@NotNull CuppaRecord cuppa) {
        assertEquals("Melanoma", cuppa.predictedCancerType());
        assertEquals(0.996, cuppa.bestPredictionLikelihood(), EPSILON);
    }

    private static void assertVirusInterpreter(@NotNull VirusInterpreterRecord virusInterpreter) {
        assertNotNull(virusInterpreter);
    }

    private static void assertChord(@NotNull ChordRecord chord) {
        assertEquals("HR_PROFICIENT", chord.hrStatus());
    }

    private static void assertProtect(@NotNull ProtectRecord protect) {
        assertEquals(138, protect.evidences().size());
    }
}