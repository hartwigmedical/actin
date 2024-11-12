package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter.isCUP
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter.lesions
import com.hartwig.actin.report.pdf.util.Formats
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class TumorDetailsInterpreterTest {

    @Test
    fun shouldCorrectlyDetermineIfTumorIsCUP() {
        assertThat(isCUP(TumorDetails())).isFalse

        assertThat(isCUP(TumorDetails(primaryTumorLocation = TumorDetailsInterpreter.CUP_LOCATION))).isFalse

        assertThat(
            isCUP(
                TumorDetails(
                    primaryTumorLocation = TumorDetailsInterpreter.CUP_LOCATION,
                    primaryTumorSubLocation = TumorDetailsInterpreter.CUP_SUB_LOCATION
                )
            )
        ).isTrue
    }

    class LesionInterpreterTest {
        @Test
        fun `Should return UNKNOWN string when no lesions`() {
            val lesions = lesions(TumorDetails())
            assertThat(lesions).isEqualTo(Formats.VALUE_UNKNOWN)
        }

        @Test
        fun `Should return string for bone lesions`() {
            val lesions = lesions(TumorDetails(hasBoneLesions = true))
            assertThat(lesions).isEqualTo(TumorDetails.BONE)
        }

        @Test
        fun `Should return string for brain lesions`() {
            val lesions = lesions(TumorDetails(hasBrainLesions = true))
            assertThat(lesions).isEqualTo(TumorDetails.BRAIN)
        }

        @Test
        fun `Should return string for cns lesions`() {
            val lesions = lesions(TumorDetails(hasCnsLesions = true))
            assertThat(lesions).isEqualTo(TumorDetails.CNS)
        }

        @Test
        fun `Should return string for liver lesions`() {
            val lesions = lesions(TumorDetails(hasLiverLesions = true))
            assertThat(lesions).isEqualTo(TumorDetails.LIVER)
        }

        @Test
        fun `Should return string for lung lesions`() {
            val lesions = lesions(TumorDetails(hasLungLesions = true))
            assertThat(lesions).isEqualTo(TumorDetails.LUNG)
        }

        @Test
        fun `Should concatenate multiple lesions together`() {
            val lesions = lesions(TumorDetails(hasLiverLesions = true, hasLungLesions = true))
            assertThat(lesions).isEqualTo("Liver, Lung")
        }

        @Test
        fun `Should combine lymph node lesions into one object with sublocations in parentheses`() {
            val details = TumorDetails(
                hasLymphNodeLesions = true,
                otherLesions = listOf("lymph nodes abdominal", "lymph nodes inguinal")
            )
            val expected = "Lymph nodes (abdominal, inguinal)"
            assertThat(lesions(details)).isEqualTo(expected)
        }

        @Test
        fun `Should return lymph nodes when input is lymph nodes without location specification`() {
            val lesions = lesions(TumorDetails(otherLesions = listOf("lymph nodes")))
            assertThat(lesions).isEqualTo("Lymph nodes")
        }

        @Test
        fun `Should map primaryTumorLocation Brain to brainLesions`() {
            val details = TumorDetails(primaryTumorLocation = "Brain")
            val expected = "Brain"
            assertThat(lesions(details)).isEqualTo(expected)
        }

        @Test
        fun `Should map primaryTumorType Glioma to brainLesions`() {
            val details = TumorDetails(primaryTumorType = "Glioma")
            val expected = "Brain"
            assertThat(lesions(details)).isEqualTo(expected)
        }

        @Test
        fun `Should include biopsyLocation among lesions locations`() {
            val details = TumorDetails(biopsyLocation = "Lung")
            val expected = "Lung"
            assertThat(lesions(details)).isEqualTo(expected)
        }

        @Test
        fun `Should put suspected lesions at the end with (suspected) postfix`() {
            val details = TumorDetails(
                hasSuspectedLiverLesions = true,
                hasLymphNodeLesions = true,
                otherSuspectedLesions = listOf("Adrenal gland", "Bladder")
            )
            val expected = "Lymph nodes, Liver (suspected), Adrenal gland (suspected), Bladder (suspected)"
            assertThat(lesions(details)).isEqualTo(expected)
        }
    }
}