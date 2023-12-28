package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SummaryChapterTest {

    @Test
    fun `Should return UNKNOWN string when no lesions`() {
        val lesions = SummaryChapter.lesions(ImmutableTumorDetails.builder().build())
        assertThat(lesions).isEqualTo("Unknown")
    }

    @Test
    fun `Should return string for bone lesions`() {
        val lesions = SummaryChapter.lesions(ImmutableTumorDetails.builder().hasBoneLesions(true).build())
        assertThat(lesions).isEqualTo("Bone")
    }

    @Test
    fun `Should return string for brain lesions`() {
        val lesions = SummaryChapter.lesions(ImmutableTumorDetails.builder().hasBrainLesions(true).build())
        assertThat(lesions).isEqualTo("Brain")
    }

    @Test
    fun `Should return string for cns lesions`() {
        val lesions = SummaryChapter.lesions(ImmutableTumorDetails.builder().hasCnsLesions(true).build())
        assertThat(lesions).isEqualTo("CNS")
    }

    @Test
    fun `Should return string for liver lesions`() {
        val lesions = SummaryChapter.lesions(ImmutableTumorDetails.builder().hasLiverLesions(true).build())
        assertThat(lesions).isEqualTo("Liver")
    }

    @Test
    fun `Should return string for lung lesions`() {
        val lesions = SummaryChapter.lesions(ImmutableTumorDetails.builder().hasLungLesions(true).build())
        assertThat(lesions).isEqualTo("Lung")
    }

    @Test
    fun `Should concatenate multiple lesions together`() {
        val lesions = SummaryChapter.lesions(ImmutableTumorDetails.builder().hasLiverLesions(true).hasLungLesions(true).build())
        assertThat(lesions).isEqualTo("Liver, Lung")
    }

    @Test
    fun `Should return string for lymph node lesions`() {
        val lesions = SummaryChapter.lesions(
            ImmutableTumorDetails.builder().addOtherLesions("lymph node first", "lymph node second").build()
        )
        assertThat(lesions).isEqualTo("Lymph nodes (first, second)")
    }

    @Test
    fun `Should return lymph nodes when input is lymph nodes without location specification`() {
        val lesions = SummaryChapter.lesions(
            ImmutableTumorDetails.builder().addOtherLesions("lymph nodes").build()
        )
        assertThat(lesions).isEqualTo("Lymph nodes")
    }
}