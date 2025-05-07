package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.characteristics.CuppaMode
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

class CharacteristicsExtractionTest {

    @Test
    fun `Should extract characteristics from minimal test data`() {
        val characteristics = CharacteristicsExtraction.extract(TestOrangeFactory.createMinimalTestOrangeRecord())

        assertThat(characteristics.purity).isNotNull()
        assertThat(characteristics.ploidy).isNotNull()
        assertThat(characteristics.predictedTumorOrigin).isNull()
        assertThat(characteristics.homologousRecombination).isNull()
        assertThat(characteristics.microsatelliteStability).isNull()
        assertThat(characteristics.tumorMutationalBurden).isNull()
        assertThat(characteristics.tumorMutationalLoad).isNull()
    }
    
    @Test
    fun `Should extract characteristics from proper test data`() {
        val characteristics = CharacteristicsExtraction.extract(TestOrangeFactory.createProperTestOrangeRecord())
        assertThat(characteristics.purity).isEqualTo(0.98, Offset.offset(EPSILON))
        assertThat(characteristics.ploidy).isEqualTo(3.1, Offset.offset(EPSILON))

        val predictedOrigin = characteristics.predictedTumorOrigin!!
        assertThat(predictedOrigin.cancerType()).isEqualTo("Melanoma")
        assertThat(predictedOrigin.likelihood()).isEqualTo(0.996, Offset.offset(EPSILON))
        assertThat(predictedOrigin.predictions.size.toLong()).isEqualTo(1)

        val cupPrediction = predictedOrigin.predictions.iterator().next()
        assertThat(cupPrediction.cancerType).isEqualTo("Melanoma")
        assertThat(cupPrediction.likelihood).isEqualTo(0.996, Offset.offset(EPSILON))
        assertThat(cupPrediction.snvPairwiseClassifier).isEqualTo(0.979, Offset.offset(EPSILON))
        assertThat(cupPrediction.genomicPositionClassifier).isEqualTo(0.99, Offset.offset(EPSILON))
        assertThat(cupPrediction.featureClassifier).isEqualTo(0.972, Offset.offset(EPSILON))
        assertThat(cupPrediction.cuppaMode).isEqualTo(CuppaMode.WGS)

        val microsatelliteStability = characteristics.microsatelliteStability!!
        assertThat(microsatelliteStability.microsatelliteIndelsPerMb).isEqualTo(0.12, Offset.offset(EPSILON))
        assertThat(microsatelliteStability.isUnstable).isFalse()

        val homologousRecombination = characteristics.homologousRecombination!!
        assertThat(homologousRecombination.score).isEqualTo(0.45, Offset.offset(EPSILON))
        assertThat(homologousRecombination.isDeficient).isFalse()
        assertThat(homologousRecombination.type).isEqualTo(HomologousRecombinationType.NONE)
        assertThat(homologousRecombination.brca1Value).isEqualTo(0.4, Offset.offset(EPSILON))
        assertThat(homologousRecombination.brca2Value).isEqualTo(0.05, Offset.offset(EPSILON))

        val tumorMutationalBurden = characteristics.tumorMutationalBurden!!
        assertThat(tumorMutationalBurden.score).isEqualTo(13.0, Offset.offset(EPSILON))
        assertThat(tumorMutationalBurden.isHigh).isTrue()

        val tumorMutationalLoad = characteristics.tumorMutationalLoad!!
        assertThat(tumorMutationalLoad.score).isEqualTo(189)
        assertThat(tumorMutationalLoad.isHigh).isTrue()
    }

    @Test
    fun `Should interpret all microsatellite instability states`() {
        val unstable = CharacteristicsExtraction.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.MSI)).microsatelliteStability!!
        assertThat(unstable.isUnstable).isTrue()

        val stable = CharacteristicsExtraction.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.MSS)).microsatelliteStability!!
        assertThat(stable.isUnstable).isFalse()

        val unknown =
            CharacteristicsExtraction.extract(withMicrosatelliteStatus(PurpleMicrosatelliteStatus.UNKNOWN)).microsatelliteStability
        assertThat(unknown).isNull()
    }

    @Test
    fun `Should set homologous recombination to null when chord is missing`() {
        val missingChord = TestOrangeFactory.createMinimalTestOrangeRecord()
        assertThat(CharacteristicsExtraction.extract(missingChord).homologousRecombination).isNull()
    }

    @Test
    fun `Should interpret all homologous recombination states`() {
        val deficient = CharacteristicsExtraction.extract(
            withHomologousRecombinationStatus(
                ChordStatus.HR_DEFICIENT,
                HomologousRecombinationType.BRCA2_TYPE
            )
        ).homologousRecombination!!

        assertThat(deficient.isDeficient).isTrue()
        assertThat(deficient.type).isEqualTo(HomologousRecombinationType.BRCA2_TYPE)

        val proficient = CharacteristicsExtraction.extract(
            withHomologousRecombinationStatus(
                ChordStatus.HR_PROFICIENT,
                HomologousRecombinationType.NONE
            )
        ).homologousRecombination!!

        assertThat(proficient.isDeficient).isFalse()
        assertThat(proficient.type).isEqualTo(HomologousRecombinationType.NONE)

        val cannotBeDetermined = CharacteristicsExtraction.extract(
            withHomologousRecombinationStatus(
                ChordStatus.CANNOT_BE_DETERMINED,
                HomologousRecombinationType.CANNOT_BE_DETERMINED
            )
        ).homologousRecombination!!

        assertThat(cannotBeDetermined.isDeficient).isNull()
        assertThat(cannotBeDetermined.type).isEqualTo(HomologousRecombinationType.CANNOT_BE_DETERMINED)

        val unknown = CharacteristicsExtraction.extract(
            withHomologousRecombinationStatus(
                ChordStatus.UNKNOWN,
                HomologousRecombinationType.CANNOT_BE_DETERMINED
            )
        ).homologousRecombination!!

        assertThat(unknown.isDeficient).isNull()
        assertThat(unknown.type).isEqualTo(HomologousRecombinationType.CANNOT_BE_DETERMINED)
    }

    @Test
    fun `Should interpret all tumor burden states`() {
        val high = CharacteristicsExtraction.extract(withTumorLoadBurden(PurpleTumorMutationalStatus.HIGH)).tumorMutationalBurden!!
        assertThat(high.isHigh).isTrue()

        val low = CharacteristicsExtraction.extract(withTumorLoadBurden(PurpleTumorMutationalStatus.LOW)).tumorMutationalBurden!!
        assertThat(low.isHigh).isFalse()

        val unknown = CharacteristicsExtraction.extract(withTumorLoadBurden(PurpleTumorMutationalStatus.UNKNOWN)).tumorMutationalBurden
        assertThat(unknown).isNull()
    }

    @Test
    fun `Should interpret all tumor load states`() {
        val high = CharacteristicsExtraction.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.HIGH)).tumorMutationalLoad!!
        assertThat(high.isHigh).isTrue()

        val low = CharacteristicsExtraction.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.LOW)).tumorMutationalLoad!!
        assertThat(low.isHigh).isFalse()

        val unknown = CharacteristicsExtraction.extract(withTumorLoadStatus(PurpleTumorMutationalStatus.UNKNOWN)).tumorMutationalLoad
        assertThat(unknown).isNull()
    }

    private fun withMicrosatelliteStatus(microsatelliteStatus: PurpleMicrosatelliteStatus): OrangeRecord {
        return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder().microsatelliteStatus(microsatelliteStatus).build())
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

    private fun withTumorLoadBurden(tumorBurdenStatus: PurpleTumorMutationalStatus): OrangeRecord {
        return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder().tumorMutationalBurdenStatus(tumorBurdenStatus).build())
    }

    private fun withTumorLoadStatus(tumorLoadStatus: PurpleTumorMutationalStatus): OrangeRecord {
        return withPurpleCharacteristics(TestPurpleFactory.characteristicsBuilder().tumorMutationalLoadStatus(tumorLoadStatus).build())
    }

    private fun withPurpleCharacteristics(characteristics: PurpleCharacteristics): OrangeRecord {
        val base = TestOrangeFactory.createMinimalTestOrangeRecord()
        return ImmutableOrangeRecord.builder()
            .from(base)
            .purple(ImmutablePurpleRecord.builder().from(base.purple()).characteristics(characteristics).build())
            .build()
    }
}