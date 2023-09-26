package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;
import com.hartwig.hmftools.datamodel.chord.ChordStatus;
import com.hartwig.hmftools.datamodel.chord.ImmutableChordRecord;
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction;
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord;
import com.hartwig.hmftools.datamodel.orange.OrangeRecord;
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.hmftools.datamodel.purple.PurpleCharacteristics;
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus;
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class CharacteristicsExtractorTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canExtractCharacteristicsFromProperTestData() {
        CharacteristicsExtractor extractor = createTestExtractor();
        MolecularCharacteristics characteristics = extractor.extract(TestOrangeFactory.createProperTestOrangeRecord());

        assertDoubleEquals(0.98, characteristics.purity());
        assertDoubleEquals(3.1, characteristics.ploidy());

        PredictedTumorOrigin predictedOrigin = characteristics.predictedTumorOrigin();
        assertNotNull(predictedOrigin);
        assertEquals("Melanoma", predictedOrigin.cancerType());
        assertEquals(0.996, predictedOrigin.likelihood(), EPSILON);
        assertEquals(1, predictedOrigin.predictions().size());
        CuppaPrediction cuppaPrediction = predictedOrigin.predictions().iterator().next();
        assertEquals("Melanoma", cuppaPrediction.cancerType());
        assertEquals(0.996, cuppaPrediction.likelihood(), EPSILON);
        assertDoubleEquals(0.979, cuppaPrediction.snvPairwiseClassifier());
        assertDoubleEquals(0.99, cuppaPrediction.genomicPositionClassifier());
        assertDoubleEquals(0.972, cuppaPrediction.featureClassifier());

        assertEquals(false, characteristics.isMicrosatelliteUnstable());
        assertNotNull(characteristics.microsatelliteEvidence());
        assertEquals(false, characteristics.isHomologousRepairDeficient());
        assertNotNull(characteristics.homologousRepairEvidence());
        assertEquals(13D, characteristics.tumorMutationalBurden(), EPSILON);
        assertEquals(true, characteristics.hasHighTumorMutationalBurden());
        assertNotNull(characteristics.tumorMutationalBurdenEvidence());
        assertEquals(189, (int) characteristics.tumorMutationalLoad());
        assertEquals(true, characteristics.hasHighTumorMutationalLoad());
        assertNotNull(characteristics.tumorMutationalLoadEvidence());
    }

    @Test
    public void canInterpretAllHomologousRepairStates() {
        CharacteristicsExtractor extractor = createTestExtractor();
        MolecularCharacteristics deficient = extractor.extract(withHomologousRepairStatus(ChordStatus.HR_DEFICIENT));
        assertTrue(deficient.isHomologousRepairDeficient());

        MolecularCharacteristics proficient = extractor.extract(withHomologousRepairStatus(ChordStatus.HR_PROFICIENT));
        assertFalse(proficient.isHomologousRepairDeficient());

        MolecularCharacteristics cannotBeDetermined = extractor.extract(withHomologousRepairStatus(ChordStatus.CANNOT_BE_DETERMINED));
        assertNull(cannotBeDetermined.isHomologousRepairDeficient());

        MolecularCharacteristics unknown = extractor.extract(withHomologousRepairStatus(ChordStatus.UNKNOWN));
        assertNull(unknown.isHomologousRepairDeficient());
    }

    @NotNull
    private static OrangeRecord withHomologousRepairStatus(@NotNull ChordStatus hrStatus) {
        return ImmutableOrangeRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord())
                .chord(ImmutableChordRecord.builder()
                        .hrStatus(hrStatus)
                        .brca1Value(0D)
                        .brca2Value(0D)
                        .hrdValue(0D)
                        .hrdType(Strings.EMPTY).build())
                .build();
    }

    @Test
    public void canInterpretAllMicrosatelliteInstabilityStates() {
        CharacteristicsExtractor extractor = createTestExtractor();
        MolecularCharacteristics unstable = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.MSI));
        assertTrue(unstable.isMicrosatelliteUnstable());

        MolecularCharacteristics stable = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.MSS));
        assertFalse(stable.isMicrosatelliteUnstable());

        MolecularCharacteristics unknown = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.UNKNOWN));
        assertNull(unknown.isMicrosatelliteUnstable());
    }

    @NotNull
    private static OrangeRecord withMicrosatelliteStatus(@NotNull PurpleMicrosatelliteStatus microsatelliteStatus) {
        return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder().microsatelliteStatus(microsatelliteStatus).build());
    }

    @Test
    public void canInterpretAllTumorLoadStates() {
        CharacteristicsExtractor extractor = createTestExtractor();
        MolecularCharacteristics high = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.HIGH));
        assertTrue(high.hasHighTumorMutationalLoad());

        MolecularCharacteristics low = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.LOW));
        assertFalse(low.hasHighTumorMutationalLoad());

        MolecularCharacteristics unknown = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.UNKNOWN));
        assertNull(unknown.hasHighTumorMutationalLoad());
    }

    private static void assertDoubleEquals(double expected, @Nullable Double actual) {
        assertNotNull(actual);
        assertEquals(expected, actual, EPSILON);
    }

    @NotNull
    private static OrangeRecord withTumorLoadStatus(@NotNull PurpleTumorMutationalStatus tumorLoadStatus) {
        return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder().tumorMutationalLoadStatus(tumorLoadStatus).build());
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