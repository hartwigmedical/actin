package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory
import com.hartwig.hmftools.datamodel.chord.ChordStatus
import com.hartwig.hmftools.datamodel.chord.ImmutableChordRecord
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleCharacteristics
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class CharacteristicsExtractorTest {
    @Test
    fun canExtractCharacteristicsFromProperTestData() {
        val extractor = createTestExtractor()
        val characteristics = extractor.extract(TestOrangeFactory.createProperTestOrangeRecord())
        assertDoubleEquals(0.98, characteristics.purity())
        assertDoubleEquals(3.1, characteristics.ploidy())
        val predictedOrigin = characteristics.predictedTumorOrigin()
        Assert.assertNotNull(predictedOrigin)
        Assert.assertEquals("Melanoma", predictedOrigin.cancerType())
        Assert.assertEquals(0.996, predictedOrigin.likelihood(), EPSILON)
        Assert.assertEquals(1, predictedOrigin.predictions().size.toLong())
        val cupPrediction = predictedOrigin.predictions().iterator().next()
        Assert.assertEquals("Melanoma", cupPrediction.cancerType())
        Assert.assertEquals(0.996, cupPrediction.likelihood(), EPSILON)
        assertDoubleEquals(0.979, cupPrediction.snvPairwiseClassifier())
        assertDoubleEquals(0.99, cupPrediction.genomicPositionClassifier())
        assertDoubleEquals(0.972, cupPrediction.featureClassifier())
        Assert.assertEquals(false, characteristics.isMicrosatelliteUnstable())
        Assert.assertNotNull(characteristics.microsatelliteEvidence())
        Assert.assertEquals(false, characteristics.isHomologousRepairDeficient())
        Assert.assertNotNull(characteristics.homologousRepairEvidence())
        Assert.assertEquals(13.0, characteristics.tumorMutationalBurden(), EPSILON)
        Assert.assertEquals(true, characteristics.hasHighTumorMutationalBurden())
        Assert.assertNotNull(characteristics.tumorMutationalBurdenEvidence())
        Assert.assertEquals(189, (characteristics.tumorMutationalLoad() as Int).toLong())
        Assert.assertEquals(true, characteristics.hasHighTumorMutationalLoad())
        Assert.assertNotNull(characteristics.tumorMutationalLoadEvidence())
    }

    @Test
    fun canInterpretAllHomologousRepairStates() {
        val extractor = createTestExtractor()
        val deficient = extractor.extract(withHomologousRepairStatus(ChordStatus.HR_DEFICIENT))
        Assert.assertTrue(deficient.isHomologousRepairDeficient())
        val proficient = extractor.extract(withHomologousRepairStatus(ChordStatus.HR_PROFICIENT))
        Assert.assertFalse(proficient.isHomologousRepairDeficient())
        val cannotBeDetermined = extractor.extract(withHomologousRepairStatus(ChordStatus.CANNOT_BE_DETERMINED))
        Assert.assertNull(cannotBeDetermined.isHomologousRepairDeficient())
        val unknown = extractor.extract(withHomologousRepairStatus(ChordStatus.UNKNOWN))
        Assert.assertNull(unknown.isHomologousRepairDeficient())
    }

    @Test
    fun canInterpretAllMicrosatelliteInstabilityStates() {
        val extractor = createTestExtractor()
        val unstable = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.MSI))
        Assert.assertTrue(unstable.isMicrosatelliteUnstable())
        val stable = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.MSS))
        Assert.assertFalse(stable.isMicrosatelliteUnstable())
        val unknown = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.UNKNOWN))
        Assert.assertNull(unknown.isMicrosatelliteUnstable())
    }

    @Test
    fun canInterpretAllTumorLoadStates() {
        val extractor = createTestExtractor()
        val high = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.HIGH))
        Assert.assertTrue(high.hasHighTumorMutationalLoad())
        val low = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.LOW))
        Assert.assertFalse(low.hasHighTumorMutationalLoad())
        val unknown = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.UNKNOWN))
        Assert.assertNull(unknown.hasHighTumorMutationalLoad())
    }

    companion object {
        private const val EPSILON = 1.0E-10
        private fun withHomologousRepairStatus(hrStatus: ChordStatus): OrangeRecord {
            return ImmutableOrangeRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord())
                .chord(ImmutableChordRecord.builder()
                    .hrStatus(hrStatus)
                    .brca1Value(0.0)
                    .brca2Value(0.0)
                    .hrdValue(0.0)
                    .hrdType(Strings.EMPTY)
                    .build())
                .build()
        }

        private fun withMicrosatelliteStatus(microsatelliteStatus: PurpleMicrosatelliteStatus): OrangeRecord {
            return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder().microsatelliteStatus(microsatelliteStatus).build())
        }

        private fun assertDoubleEquals(expected: Double, actual: Double?) {
            Assert.assertNotNull(actual)
            Assert.assertEquals(expected, actual, EPSILON)
        }

        private fun withTumorLoadStatus(tumorLoadStatus: PurpleTumorMutationalStatus): OrangeRecord {
            return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder().tumorMutationalLoadStatus(tumorLoadStatus).build())
        }

        private fun createTestExtractor(): CharacteristicsExtractor {
            return CharacteristicsExtractor(TestEvidenceDatabaseFactory.createEmptyDatabase())
        }

        private fun withPurpleCharacteristics(characteristics: PurpleCharacteristics): OrangeRecord {
            val base = TestOrangeFactory.createMinimalTestOrangeRecord()
            return ImmutableOrangeRecord.builder()
                .from(base)
                .purple(ImmutablePurpleRecord.builder().from(base.purple()).characteristics(characteristics).build())
                .build()
        }
    }
}