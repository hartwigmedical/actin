package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.chord.ImmutableChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCharacteristics;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CharacteristicsExtractorTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canExtractCharacteristicsFromProperTestData() {
        CharacteristicsExtractor extractor = createTestExtractor();
        MolecularCharacteristics characteristics = extractor.extract(TestOrangeFactory.createProperTestOrangeRecord());

        assertEquals(0.98, characteristics.purity(), EPSILON);
        assertEquals(3.1, characteristics.ploidy(), EPSILON);
        assertEquals("Melanoma", characteristics.predictedTumorOrigin().tumorType());
        assertEquals(0.996, characteristics.predictedTumorOrigin().likelihood(), EPSILON);
        assertFalse(characteristics.isMicrosatelliteUnstable());
        assertNotNull(characteristics.microsatelliteEvidence());
        assertFalse(characteristics.isHomologousRepairDeficient());
        assertNotNull(characteristics.homologousRepairEvidence());
        assertEquals(13D, characteristics.tumorMutationalBurden(), EPSILON);
        assertTrue(characteristics.hasHighTumorMutationalBurden());
        assertNotNull(characteristics.tumorMutationalBurdenEvidence());
        assertEquals(189, (int) characteristics.tumorMutationalLoad());
        assertTrue(characteristics.hasHighTumorMutationalLoad());
        assertNotNull(characteristics.tumorMutationalLoadEvidence());
    }

    @Test
    public void canInterpretAllHomologousRepairStates() {
        CharacteristicsExtractor extractor = createTestExtractor();
        MolecularCharacteristics deficient =
                extractor.extract(withHomologousRepairStatus(CharacteristicsExtractor.HOMOLOGOUS_REPAIR_DEFICIENT));
        assertTrue(deficient.isHomologousRepairDeficient());

        MolecularCharacteristics proficient =
                extractor.extract(withHomologousRepairStatus(CharacteristicsExtractor.HOMOLOGOUS_REPAIR_PROFICIENT));
        assertFalse(proficient.isHomologousRepairDeficient());

        MolecularCharacteristics unknown =
                extractor.extract(withHomologousRepairStatus(CharacteristicsExtractor.HOMOLOGOUS_REPAIR_UNKNOWN));
        assertNull(unknown.isHomologousRepairDeficient());

        MolecularCharacteristics weird = extractor.extract(withHomologousRepairStatus("not a valid status"));
        assertNull(weird.isHomologousRepairDeficient());
    }

    @NotNull
    private static OrangeRecord withHomologousRepairStatus(@NotNull String hrStatus) {
        return ImmutableOrangeRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord())
                .chord(ImmutableChordRecord.builder().hrStatus(hrStatus).build())
                .build();
    }

    @Test
    public void canInterpretAllMicrosatelliteInstabilityStates() {
        CharacteristicsExtractor extractor = createTestExtractor();
        MolecularCharacteristics unstable = extractor.extract(withMicrosatelliteStatus(CharacteristicsExtractor.MICROSATELLITE_UNSTABLE));
        assertTrue(unstable.isMicrosatelliteUnstable());

        MolecularCharacteristics stable = extractor.extract(withMicrosatelliteStatus(CharacteristicsExtractor.MICROSATELLITE_STABLE));
        assertFalse(stable.isMicrosatelliteUnstable());

        MolecularCharacteristics weird = extractor.extract(withMicrosatelliteStatus("not a valid status"));
        assertNull(weird.isMicrosatelliteUnstable());
    }

    @NotNull
    private static OrangeRecord withMicrosatelliteStatus(@NotNull String microsatelliteStatus) {
        return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder()
                .microsatelliteStabilityStatus(microsatelliteStatus)
                .build());
    }

    @Test
    public void canInterpretAllTumorLoadStates() {
        CharacteristicsExtractor extractor = createTestExtractor();
        MolecularCharacteristics high = extractor.extract(withTumorLoadStatus(CharacteristicsExtractor.TUMOR_STATUS_HIGH));
        assertTrue(high.hasHighTumorMutationalLoad());

        MolecularCharacteristics low = extractor.extract(withTumorLoadStatus(CharacteristicsExtractor.TUMOR_STATUS_LOW));
        assertFalse(low.hasHighTumorMutationalLoad());

        MolecularCharacteristics unknown = extractor.extract(withTumorLoadStatus(CharacteristicsExtractor.TUMOR_STATUS_UNKNOWN));
        assertNull(unknown.hasHighTumorMutationalLoad());

        MolecularCharacteristics weird = extractor.extract(withTumorLoadStatus("not a valid status"));
        assertNull(weird.hasHighTumorMutationalLoad());
    }

    @NotNull
    private static OrangeRecord withTumorLoadStatus(@NotNull String tumorLoadStatus) {
        return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder()
                .tumorMutationalLoadStatus(tumorLoadStatus)
                .build());
    }

    @NotNull
    private static CharacteristicsExtractor createTestExtractor() {
        return new CharacteristicsExtractor(TestEvidenceDatabaseFactory.createEmptyDatabase());
    }

    @NotNull
    private static OrangeRecord withPurpleCharacteristics(@NotNull PurpleCharacteristics characteristics) {
        OrangeRecord base = TestOrangeFactory.createMinimalTestOrangeRecord();

        return ImmutableOrangeRecord.builder()
                .from(base)
                .purple(ImmutablePurpleRecord.builder().from(base.purple()).characteristics(characteristics).build())
                .build();
    }
}