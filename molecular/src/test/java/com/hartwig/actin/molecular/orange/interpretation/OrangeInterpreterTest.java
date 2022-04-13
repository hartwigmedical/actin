package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeDataFactory;
import com.hartwig.actin.molecular.orange.datamodel.chord.ImmutableChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeInterpreterTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canCreateInterpreterFromEmptyServeRecords() {
        assertNotNull(OrangeInterpreter.fromServeRecords(Lists.newArrayList()));
    }

    @Test
    public void canInterpretOrangeRecord() {
        MolecularRecord record = createTestInterpreter().interpret(TestOrangeDataFactory.createProperTestOrangeRecord());

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertEquals(ExperimentType.WGS, record.type());
        assertEquals(LocalDate.of(2021, 5, 6), record.date());
        assertTrue(record.hasReliableQuality());

        MolecularCharacteristics characteristics = record.characteristics();
        assertEquals(0.98, characteristics.purity(), EPSILON);
        assertTrue(characteristics.hasReliablePurity());
        assertEquals("Melanoma", characteristics.predictedTumorOrigin().tumorType());
        assertEquals(0.996, characteristics.predictedTumorOrigin().likelihood(), EPSILON);
        assertFalse(characteristics.isMicrosatelliteUnstable());
        assertFalse(characteristics.isHomologousRepairDeficient());
        assertEquals(13D, characteristics.tumorMutationalBurden(), EPSILON);
        assertEquals(189, (int) characteristics.tumorMutationalLoad());

        MolecularDrivers drivers = record.drivers();
        assertEquals(1, drivers.variants().size());
        assertEquals(1, drivers.amplifications().size());
        assertEquals(1, drivers.losses().size());
        assertEquals(2, drivers.disruptions().size());
        assertEquals(1, drivers.fusions().size());
        assertEquals(1, drivers.viruses().size());

        assertEquals(1, record.pharmaco().size());

        MolecularEvidence evidence = record.evidence();
        assertEquals(1, evidence.actinTrials().size());
        assertEquals(1, evidence.externalTrials().size());
        assertEquals(1, evidence.approvedResponsiveEvidence().size());
        assertEquals(0, evidence.resistanceEvidence().size());
    }

    @Test
    public void canInterpretAllHomologousRepairStates() {
        OrangeInterpreter interpreter = createTestInterpreter();
        MolecularRecord deficient = interpreter.interpret(withHomologousRepairStatus(OrangeInterpreter.HOMOLOGOUS_REPAIR_DEFICIENT));
        assertTrue(deficient.characteristics().isHomologousRepairDeficient());

        MolecularRecord proficient = interpreter.interpret(withHomologousRepairStatus(OrangeInterpreter.HOMOLOGOUS_REPAIR_PROFICIENT));
        assertFalse(proficient.characteristics().isHomologousRepairDeficient());

        MolecularRecord unknown = interpreter.interpret(withHomologousRepairStatus(OrangeInterpreter.HOMOLOGOUS_REPAIR_UNKNOWN));
        assertNull(unknown.characteristics().isHomologousRepairDeficient());

        MolecularRecord weird = interpreter.interpret(withHomologousRepairStatus("not a valid status"));
        assertNull(weird.characteristics().isHomologousRepairDeficient());
    }

    @NotNull
    private static OrangeRecord withHomologousRepairStatus(@NotNull String hrStatus) {
        return ImmutableOrangeRecord.builder()
                .from(TestOrangeDataFactory.createMinimalTestOrangeRecord())
                .chord(ImmutableChordRecord.builder().hrStatus(hrStatus).build())
                .build();
    }

    @Test
    public void canInterpretAllMicrosatelliteInstabilityStates() {
        OrangeInterpreter interpreter = createTestInterpreter();
        MolecularRecord unstable = interpreter.interpret(withMicrosatelliteStatus(OrangeInterpreter.MICROSATELLITE_UNSTABLE));
        assertTrue(unstable.characteristics().isMicrosatelliteUnstable());

        MolecularRecord stable = interpreter.interpret(withMicrosatelliteStatus(OrangeInterpreter.MICROSATELLITE_STABLE));
        assertFalse(stable.characteristics().isMicrosatelliteUnstable());

        MolecularRecord weird = interpreter.interpret(withMicrosatelliteStatus("not a valid status"));
        assertNull(weird.characteristics().isMicrosatelliteUnstable());
    }

    @NotNull
    private static OrangeRecord withMicrosatelliteStatus(@NotNull String microsatelliteStatus) {
        OrangeRecord base = TestOrangeDataFactory.createMinimalTestOrangeRecord();

        return ImmutableOrangeRecord.builder()
                .from(base)
                .purple(ImmutablePurpleRecord.builder().from(base.purple()).microsatelliteStabilityStatus(microsatelliteStatus).build())
                .build();
    }

    @NotNull
    private static OrangeInterpreter createTestInterpreter() {
        OrangeEventMapper testEventExtractor = new OrangeEventMapper(evidence -> Sets.newHashSet(evidence.event()));
        OrangeEvidenceFactory testEvidenceFactory = new OrangeEvidenceFactory(evidence -> true);
        return new OrangeInterpreter(testEventExtractor, testEvidenceFactory);
    }
}