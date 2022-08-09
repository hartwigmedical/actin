package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.chord.ImmutableChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CharacteristicsExtractionTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canExtractCharacteristics() {
        MolecularCharacteristics characteristics = CharacteristicsExtraction.extract(TestOrangeFactory.createProperTestOrangeRecord());

        assertEquals(0.98, characteristics.purity(), EPSILON);
        assertEquals("Melanoma", characteristics.predictedTumorOrigin().tumorType());
        assertEquals(0.996, characteristics.predictedTumorOrigin().likelihood(), EPSILON);
        assertFalse(characteristics.isMicrosatelliteUnstable());
        assertFalse(characteristics.isHomologousRepairDeficient());
        assertEquals(13D, characteristics.tumorMutationalBurden(), EPSILON);
        assertEquals(189, (long) characteristics.tumorMutationalLoad());
    }

    @Test
    public void canInterpretAllHomologousRepairStates() {
        MolecularCharacteristics deficient =
                CharacteristicsExtraction.extract(withHomologousRepairStatus(CharacteristicsExtraction.HOMOLOGOUS_REPAIR_DEFICIENT));
        assertTrue(deficient.isHomologousRepairDeficient());

        MolecularCharacteristics proficient =
                CharacteristicsExtraction.extract(withHomologousRepairStatus(CharacteristicsExtraction.HOMOLOGOUS_REPAIR_PROFICIENT));
        assertFalse(proficient.isHomologousRepairDeficient());

        MolecularCharacteristics unknown =
                CharacteristicsExtraction.extract(withHomologousRepairStatus(CharacteristicsExtraction.HOMOLOGOUS_REPAIR_UNKNOWN));
        assertNull(unknown.isHomologousRepairDeficient());

        MolecularCharacteristics weird = CharacteristicsExtraction.extract(withHomologousRepairStatus("not a valid status"));
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
        MolecularCharacteristics unstable =
                CharacteristicsExtraction.extract(withMicrosatelliteStatus(CharacteristicsExtraction.MICROSATELLITE_UNSTABLE));
        assertTrue(unstable.isMicrosatelliteUnstable());

        MolecularCharacteristics stable =
                CharacteristicsExtraction.extract(withMicrosatelliteStatus(CharacteristicsExtraction.MICROSATELLITE_STABLE));
        assertFalse(stable.isMicrosatelliteUnstable());

        MolecularCharacteristics weird = CharacteristicsExtraction.extract(withMicrosatelliteStatus("not a valid status"));
        assertNull(weird.isMicrosatelliteUnstable());
    }

    @NotNull
    private static OrangeRecord withMicrosatelliteStatus(@NotNull String microsatelliteStatus) {
        OrangeRecord base = TestOrangeFactory.createMinimalTestOrangeRecord();

        return ImmutableOrangeRecord.builder()
                .from(base)
                .purple(ImmutablePurpleRecord.builder().from(base.purple()).microsatelliteStabilityStatus(microsatelliteStatus).build())
                .build();
    }
}