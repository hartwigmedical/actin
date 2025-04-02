package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
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

private const val EPSILON = 1.0E-10

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
        val hrScore = characteristics.homologousRecombinationScore
        assertThat(hrScore).isNotNull()
        assertThat(hrScore!!).isEqualTo(0.45, Offset.offset(EPSILON))
        assertThat(characteristics.isHomologousRecombinationDeficient).isFalse()
        assertThat(characteristics.homologousRecombinationEvidence).isNotNull()

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
    fun `Should interpret all homologous recombination states`() {
        val extractor = createTestExtractor()

        val deficient =
            extractor.extract(withHomologousRecombinationStatus(ChordStatus.HR_DEFICIENT, HomologousRecombinationType.BRCA2_TYPE))
        assertThat(deficient.isHomologousRecombinationDeficient).isTrue()
        assertThat(deficient.homologousRecombinationType).isEqualTo(HomologousRecombinationType.BRCA2_TYPE)

        val proficient = extractor.extract(withHomologousRecombinationStatus(ChordStatus.HR_PROFICIENT, HomologousRecombinationType.NONE))
        assertThat(proficient.isHomologousRecombinationDeficient).isFalse()
        assertThat(proficient.homologousRecombinationType).isEqualTo(HomologousRecombinationType.NONE)

        val cannotBeDetermined =
            extractor.extract(withHomologousRecombinationStatus(ChordStatus.CANNOT_BE_DETERMINED, HomologousRecombinationType.BRCA1_TYPE))
        assertThat(cannotBeDetermined.isHomologousRecombinationDeficient).isNull()
        assertThat(cannotBeDetermined.homologousRecombinationType).isEqualTo(HomologousRecombinationType.BRCA1_TYPE)

        val unknown =
            extractor.extract(withHomologousRecombinationStatus(ChordStatus.UNKNOWN, HomologousRecombinationType.CANNOT_BE_DETERMINED))
        assertThat(unknown.isHomologousRecombinationDeficient).isNull()
        assertThat(unknown.homologousRecombinationType).isEqualTo(HomologousRecombinationType.CANNOT_BE_DETERMINED)
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

    private fun withHomologousRecombinationStatus(
        hrStatus: ChordStatus,
        homologousRecombinationType: HomologousRecombinationType
    ): OrangeRecord {
        return ImmutableOrangeRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord())
            .chord(
                ImmutableChordRecord.builder()
                    .hrStatus(hrStatus)
                    .brca1Value(0.0)
                    .brca2Value(0.0)
                    .hrdValue(0.0)
                    .hrdType(homologousRecombinationType.name)
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
}