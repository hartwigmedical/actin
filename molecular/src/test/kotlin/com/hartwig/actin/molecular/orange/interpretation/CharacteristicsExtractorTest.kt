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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacteristicsExtractorTest {

    @Test
    fun canExtractCharacteristicsFromProperTestData() {
        val extractor = createTestExtractor()
        val characteristics = extractor.extract(TestOrangeFactory.createProperTestOrangeRecord())
        assertDoubleEquals(0.98, characteristics.purity())
        assertDoubleEquals(3.1, characteristics.ploidy())

        val predictedOrigin = characteristics.predictedTumorOrigin()
        assertNotNull(predictedOrigin)

        assertEquals("Melanoma", predictedOrigin!!.cancerType())
        assertEquals(0.996, predictedOrigin.likelihood(), EPSILON)
        assertEquals(1, predictedOrigin.predictions().size.toLong())

        val cupPrediction = predictedOrigin.predictions().iterator().next()
        assertEquals("Melanoma", cupPrediction.cancerType())
        assertEquals(0.996, cupPrediction.likelihood(), EPSILON)
        assertDoubleEquals(0.979, cupPrediction.snvPairwiseClassifier())
        assertDoubleEquals(0.99, cupPrediction.genomicPositionClassifier())
        assertDoubleEquals(0.972, cupPrediction.featureClassifier())

        assertEquals(false, characteristics.isMicrosatelliteUnstable())
        assertNotNull(characteristics.microsatelliteEvidence())
        val hrScore = characteristics.homologousRepairScore()
        assertNotNull(hrScore)
        assertEquals(0.45, hrScore!!, EPSILON)
        assertEquals(false, characteristics.isHomologousRepairDeficient())
        assertNotNull(characteristics.homologousRepairEvidence())

        val tmb = characteristics.tumorMutationalBurden()
        assertNotNull(tmb)
        assertEquals(13.0, tmb!!, EPSILON)
        assertEquals(true, characteristics.hasHighTumorMutationalBurden())
        assertNotNull(characteristics.tumorMutationalBurdenEvidence())
        assertEquals(189, (characteristics.tumorMutationalLoad() as Int).toLong())
        assertEquals(true, characteristics.hasHighTumorMutationalLoad())
        assertNotNull(characteristics.tumorMutationalLoadEvidence())
    }

    @Test
    fun canInterpretAllHomologousRepairStates() {
        val extractor = createTestExtractor()

        val deficient = extractor.extract(withHomologousRepairStatus(ChordStatus.HR_DEFICIENT))
        assertTrue(deficient.isHomologousRepairDeficient() == true)

        val proficient = extractor.extract(withHomologousRepairStatus(ChordStatus.HR_PROFICIENT))
        assertFalse(proficient.isHomologousRepairDeficient() == true)

        val cannotBeDetermined = extractor.extract(withHomologousRepairStatus(ChordStatus.CANNOT_BE_DETERMINED))
        assertNull(cannotBeDetermined.isHomologousRepairDeficient())

        val unknown = extractor.extract(withHomologousRepairStatus(ChordStatus.UNKNOWN))
        assertNull(unknown.isHomologousRepairDeficient())
    }

    @Test
    fun canInterpretAllMicrosatelliteInstabilityStates() {
        val extractor = createTestExtractor()

        val unstable = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.MSI))
        assertTrue(unstable.isMicrosatelliteUnstable() == true)

        val stable = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.MSS))
        assertFalse(stable.isMicrosatelliteUnstable() == true)

        val unknown = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.UNKNOWN))
        assertNull(unknown.isMicrosatelliteUnstable())
    }

    @Test
    fun canInterpretAllTumorLoadStates() {
        val extractor = createTestExtractor()

        val high = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.HIGH))
        assertTrue(high.hasHighTumorMutationalLoad() == true)

        val low = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.LOW))
        assertFalse(low.hasHighTumorMutationalLoad() == true)

        val unknown = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.UNKNOWN))
        assertNull(unknown.hasHighTumorMutationalLoad())
    }

    companion object {
        private const val EPSILON = 1.0E-10

        private fun withHomologousRepairStatus(hrStatus: ChordStatus): OrangeRecord {
            return ImmutableOrangeRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord())
                .chord(
                    ImmutableChordRecord.builder()
                        .hrStatus(hrStatus)
                        .brca1Value(0.0)
                        .brca2Value(0.0)
                        .hrdValue(0.0)
                        .hrdType(Strings.EMPTY)
                        .build()
                )
                .build()
        }

        private fun withMicrosatelliteStatus(microsatelliteStatus: PurpleMicrosatelliteStatus): OrangeRecord {
            return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder().microsatelliteStatus(microsatelliteStatus).build())
        }

        private fun assertDoubleEquals(expected: Double, actual: Double?) {
            assertNotNull(actual)
            assertEquals(expected, actual!!, EPSILON)
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