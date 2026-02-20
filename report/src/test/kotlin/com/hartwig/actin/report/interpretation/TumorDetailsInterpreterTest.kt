package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter.classifyLesions
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter.hasCancerOfUnknownPrimary
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter.lesionString
import com.hartwig.actin.report.pdf.util.Formats
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class TumorDetailsInterpreterTest {

    @Test
    fun `Should correctly determine if tumor is CUP`() {
        assertThat(hasCancerOfUnknownPrimary(TumorDetails().name)).isFalse
        assertThat(hasCancerOfUnknownPrimary(TumorDetails(name = "Unknown").name)).isFalse
        assertThat(hasCancerOfUnknownPrimary(TumorDetails(name = "Something (CUP))").name)).isTrue()
    }

    class LesionInterpreterTest {

        @Test
        fun `Should return unknown string when lesion data is missing`() {
            val lesions = lesionString(TumorDetails())
            assertThat(lesions).isEqualTo(Formats.VALUE_UNKNOWN)
        }

        @Test
        fun `Should return string for bone lesions`() {
            val lesions = lesionString(TumorDetails(hasBoneLesions = true))
            assertThat(lesions).isEqualTo(TumorDetails.BONE)
        }

        @Test
        fun `Should return string for brain lesions`() {
            val lesions = lesionString(TumorDetails(hasBrainLesions = true))
            assertThat(lesions).isEqualTo(TumorDetails.BRAIN)
        }

        @Test
        fun `Should return string for cns lesions`() {
            val lesions = lesionString(TumorDetails(hasCnsLesions = true))
            assertThat(lesions).isEqualTo(TumorDetails.CNS)
        }

        @Test
        fun `Should return string for liver lesions`() {
            val lesions = lesionString(TumorDetails(hasLiverLesions = true))
            assertThat(lesions).isEqualTo(TumorDetails.LIVER)
        }

        @Test
        fun `Should return string for lung lesions`() {
            val lesions = lesionString(TumorDetails(hasLungLesions = true))
            assertThat(lesions).isEqualTo(TumorDetails.LUNG)
        }

        @Test
        fun `Should concatenate multiple lesions together`() {
            val lesions = lesionString(TumorDetails(hasLiverLesions = true, hasLungLesions = true))
            assertThat(lesions).isEqualTo("Liver, Lung")
        }

        @Test
        fun `Should combine lymph node lesions into one object with sub-locations in parentheses`() {
            val details = TumorDetails(
                hasLymphNodeLesions = true,
                otherLesions = listOf("lymph nodes abdominal", "lymph nodes inguinal")
            )
            val expected = "Lymph nodes (abdominal, inguinal)"
            assertThat(lesionString(details)).isEqualTo(expected)
        }

        @Test
        fun `Should return lymph nodes when input is lymph nodes without location specification`() {
            val lesions = lesionString(TumorDetails(otherLesions = listOf("lymph nodes")))
            assertThat(lesions).isEqualTo("Lymph nodes")
        }

        @Test
        fun `Should return capitalized omental string`() {
            val lesions = lesionString(TumorDetails(otherLesions = listOf("omental")))
            assertThat(lesions).isEqualTo("Omental")
        }

        @Test
        fun `Should map name Brain to brainLesions`() {
            val details = TumorDetails(name = "Some brain")
            val expected = "Brain"
            assertThat(lesionString(details)).isEqualTo(expected)
        }

        @Test
        fun `Should map name Glioma to brainLesions`() {
            val details = TumorDetails(name = "Some glioma")
            val expected = "Brain"
            assertThat(lesionString(details)).isEqualTo(expected)
        }

        @Test
        fun `Should include biopsyLocation among lesions locations`() {
            val details = TumorDetails(biopsyLocation = "Lung")
            val expected = "Lung"
            assertThat(lesionString(details)).isEqualTo(expected)
        }

        @Test
        fun `Should show (active) postfix if active CNS or brain lesion`() {
            val details =
                TumorDetails(hasActiveBrainLesions = true, hasBrainLesions = true, hasCnsLesions = true, hasActiveCnsLesions = true)
            assertThat(lesionString(details)).isEqualTo("Brain (active), CNS (active)")
        }

        @Test
        fun `Should only show lesion once and without (suspected) if both suspected and confirmed`() {
            val details = TumorDetails(hasBoneLesions = true, hasSuspectedBoneLesions = true)
            assertThat(lesionString(details)).isEqualTo("Bone")
        }

        @Test
        fun `Should put suspected lesions at the end with (suspected) postfix`() {
            val details = TumorDetails(
                hasBoneLesions = true,
                hasSuspectedLiverLesions = true,
                hasLymphNodeLesions = true,
                otherLesions = listOf("Lymph nodes inguinal", "Lymph nodes mediastinal"),
                otherSuspectedLesions = listOf("Adrenal gland", "Bladder")
            )
            val expected = "Bone, Lymph nodes (inguinal, mediastinal), Liver (suspected), Adrenal gland (suspected), Bladder (suspected)"
            assertThat(lesionString(details)).isEqualTo(expected)
        }

        @Test
        fun `Should only show lymph node lesions once if both in suspected and other suspected lesions`() {
            val details = TumorDetails(
                hasSuspectedLymphNodeLesions = true,
                otherSuspectedLesions = listOf("Lymph nodes")
            )
            val expected = "Lymph nodes (suspected)"
            assertThat(lesionString(details)).isEqualTo(expected)
        }

        @Test
        fun `Should correctly sort lesions in lesion object`() {
            val details = TumorDetails(
                hasBoneLesions = true,
                hasLiverLesions = false,
                hasSuspectedLungLesions = true,
                hasBrainLesions = null,
                hasActiveBrainLesions = null,
                hasSuspectedBrainLesions = null,
                hasCnsLesions = true,
                hasLymphNodeLesions = true,
                otherLesions = listOf("Lymph nodes inguinal", "Lymph nodes mediastinal"),
                otherSuspectedLesions = listOf("Adrenal gland")
            )
            val expected = TumorDetailsInterpreter.Lesions(
                nonLymphNodeLesions = listOf("Bone", "CNS"),
                lymphNodeLesions = listOf("Lymph nodes (inguinal, mediastinal)"),
                suspectedLesions = listOf("Lung (suspected)", "Adrenal gland (suspected)"),
                negativeCategories = listOf("Liver")
            )
            assertThat(classifyLesions(details)).isEqualTo(expected)
        }
    }
}