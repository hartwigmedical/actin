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

    private static final String MINIMALLY_EMPTY_ORANGE_JSON = Resources.getResource("orange/minimally.empty.orange.json").getPath();
    private static final String MINIMALLY_POPULATED_ORANGE_JSON = Resources.getResource("orange/minimally.populated.orange.json").getPath();
    private static final String REAL_ORANGE_JSON = Resources.getResource("orange/real.orange.json").getPath();

    private static final double EPSILON = 1.0E-2;

    @Test
    public void canReadMinimallyEmptyOrangeRecordJson() throws IOException {
        assertNotNull(OrangeJson.read(MINIMALLY_EMPTY_ORANGE_JSON));
    }

    @Test
    public void canReadRealOrangeRecordJson() throws IOException {
        assertNotNull(OrangeJson.read(REAL_ORANGE_JSON));
    }

    @Test
    public void canReadMinimallyPopulatedOrangeRecordJson() throws IOException {
        OrangeRecord record = OrangeJson.read(MINIMALLY_POPULATED_ORANGE_JSON);

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

        assertEquals(2, purple.variants().size());

        assertEquals(1, purple.gainsLosses().size());
    }

    private static void assertLinx(@NotNull LinxRecord linx) {
        assertEquals(1, linx.fusions().size());
        assertEquals(1, linx.homozygousDisruptedGenes().size());
        assertEquals(1, linx.disruptions().size());
    }

    private static void assertPeach(@NotNull PeachRecord peach) {
        assertEquals(1, peach.entries().size());
    }

    private static void assertCuppa(@NotNull CuppaRecord cuppa) {
        assertEquals("Melanoma", cuppa.predictedCancerType());
        assertEquals(0.996, cuppa.bestPredictionLikelihood(), EPSILON);
    }

    private static void assertVirusInterpreter(@NotNull VirusInterpreterRecord virusInterpreter) {
        assertEquals(1, virusInterpreter.entries().size());
    }

    private static void assertChord(@NotNull ChordRecord chord) {
        assertEquals("HR_PROFICIENT", chord.hrStatus());
    }

    private static void assertProtect(@NotNull ProtectRecord protect) {
        assertEquals(1, protect.evidences().size());
    }
}