package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.datamodel.molecular.HrdType
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.chord.ChordStatus
import com.hartwig.hmftools.datamodel.chord.ImmutableChordRecord
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleCharacteristics
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test

class CharacteristicsExtractorTest {

    @Test
    fun `Should extract characteristics from proper test data`() {
        val extractor = createTestExtractor()
        val characteristics = extractor.extract(TestOrangeFactory.createProperTestOrangeRecord())
        assertThat(characteristics.purity).isEqualTo(0.98, Offset.offset(EPSILON))
        assertThat(characteristics.ploidy).isEqualTo(3.1, Offset.offset(EPSILON))

        val predictedOrigin = characteristics.predictedTumorOrigin
        assertThat(predictedOrigin).isNotNull()

        assertThat(predictedOrigin!!.cancerType()).isEqualTo("Melanoma")
        assertThat(predictedOrigin.likelihood()).isEqualTo(0.996, Offset.offset(EPSILON))
        assertThat(predictedOrigin.predictions.size.toLong()).isEqualTo(1)

        val cupPrediction = predictedOrigin.predictions.iterator().next()
        assertThat(cupPrediction.cancerType).isEqualTo("Melanoma")
        assertThat(cupPrediction.likelihood).isEqualTo(0.996, Offset.offset(EPSILON))
        assertThat(cupPrediction.snvPairwiseClassifier).isEqualTo(0.979, Offset.offset(EPSILON))
        assertThat(cupPrediction.genomicPositionClassifier).isEqualTo(0.99, Offset.offset(EPSILON))
        assertThat(cupPrediction.featureClassifier).isEqualTo(0.972, Offset.offset(EPSILON))

        assertThat(characteristics.isMicrosatelliteUnstable).isFalse()
        assertThat(characteristics.microsatelliteEvidence).isNotNull()
        val hrScore = characteristics.homologousRepairScore
        assertThat(hrScore).isNotNull()
        assertThat(hrScore!!).isEqualTo(0.45, Offset.offset(EPSILON))
        assertThat(characteristics.isHomologousRepairDeficient).isFalse()
        assertThat(characteristics.homologousRepairEvidence).isNotNull()

        val tmb = characteristics.tumorMutationalBurden
        assertThat(tmb).isNotNull()
        assertThat(tmb!!).isEqualTo(13.0, Offset.offset(EPSILON))
        assertThat(characteristics.hasHighTumorMutationalBurden).isTrue()
        assertThat(characteristics.tumorMutationalBurdenEvidence).isNotNull()
        assertThat(characteristics.tumorMutationalLoad).isEqualTo(189)
        assertThat(characteristics.hasHighTumorMutationalLoad).isTrue()
        assertThat(characteristics.tumorMutationalLoadEvidence).isNotNull()
    }

    @Test
    fun `Should interpret all homologous repair states`() {
        val extractor = createTestExtractor()

        val deficient = extractor.extract(withHomologousRepairStatus(ChordStatus.HR_DEFICIENT, HrdType.BRCA2_TYPE))
        assertThat(deficient.isHomologousRepairDeficient).isTrue()
        assertThat(deficient.hrdType).isEqualTo(HrdType.BRCA2_TYPE)

        val proficient = extractor.extract(withHomologousRepairStatus(ChordStatus.HR_PROFICIENT))
        assertThat(proficient.isHomologousRepairDeficient).isFalse()

        val cannotBeDetermined = extractor.extract(withHomologousRepairStatus(ChordStatus.CANNOT_BE_DETERMINED, HrdType.BRCA1_TYPE))
        assertThat(cannotBeDetermined.isHomologousRepairDeficient).isNull()
        assertThat(cannotBeDetermined.hrdType).isEqualTo(HrdType.BRCA1_TYPE)

        val unknown = extractor.extract(withHomologousRepairStatus(ChordStatus.UNKNOWN))
        assertThat(unknown.isHomologousRepairDeficient).isNull()
    }

    @Test
    fun `Should interpret all microsatellite instability states`() {
        val extractor = createTestExtractor()

        val unstable = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.MSI))
        assertThat(unstable.isMicrosatelliteUnstable).isTrue()

        val stable = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.MSS))
        assertThat(stable.isMicrosatelliteUnstable).isFalse()

        val unknown = extractor.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.UNKNOWN))
        assertThat(unknown.isMicrosatelliteUnstable).isNull()
    }

    @Test
    fun `Should interpret all tumor load states`() {
        val extractor = createTestExtractor()

        val high = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.HIGH))
        assertThat(high.hasHighTumorMutationalLoad).isTrue()

        val low = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.LOW))
        assertThat(low.hasHighTumorMutationalLoad).isFalse()

        val unknown = extractor.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.UNKNOWN))
        assertThat(unknown.hasHighTumorMutationalLoad).isNull()
    }

    private fun withHomologousRepairStatus(hrStatus: ChordStatus, hrdType: HrdType = HrdType.NONE): OrangeRecord {
        return ImmutableOrangeRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord())
            .chord(
                ImmutableChordRecord.builder()
                    .hrStatus(hrStatus)
                    .brca1Value(0.0)
                    .brca2Value(0.0)
                    .hrdValue(0.0)
                    .hrdType(hrdType.name)
                    .build()
            )
            .build()
    }

    private fun withMicrosatelliteStatus(microsatelliteStatus: PurpleMicrosatelliteStatus): OrangeRecord {
        return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder().microsatelliteStatus(microsatelliteStatus).build())
    }

    private fun withTumorLoadStatus(tumorLoadStatus: PurpleTumorMutationalStatus): OrangeRecord {
        return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder().tumorMutationalLoadStatus(tumorLoadStatus).build())
    }

    private fun createTestExtractor(): CharacteristicsExtractor {
        return CharacteristicsExtractor()
    }

    private fun withPurpleCharacteristics(characteristics: PurpleCharacteristics): OrangeRecord {
        val base = TestOrangeFactory.createMinimalTestOrangeRecord()
        return ImmutableOrangeRecord.builder()
            .from(base)
            .purple(ImmutablePurpleRecord.builder().from(base.purple()).characteristics(characteristics).build())
            .build()
    }

    companion object {
        private const val EPSILON = 1.0E-10
    }
}