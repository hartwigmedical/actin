package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.ReportFunctions.lesions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SummaryChapterTest {

    @Test
    fun `Should return UNKNOWN string when no lesions`() {
        val lesions = lesions(TumorDetails())
        assertThat(lesions).isEqualTo("Unknown")
    }

    @Test
    fun `Should return string for bone lesions`() {
        val lesions = lesions(TumorDetails(hasBoneLesions = true))
        assertThat(lesions).isEqualTo("Bone")
    }

    @Test
    fun `Should return string for brain lesions`() {
        val lesions = lesions(TumorDetails(hasBrainLesions = true))
        assertThat(lesions).isEqualTo("Brain")
    }

    @Test
    fun `Should return string for cns lesions`() {
        val lesions = lesions(TumorDetails(hasCnsLesions = true))
        assertThat(lesions).isEqualTo("CNS")
    }

    @Test
    fun `Should return string for liver lesions`() {
        val lesions = lesions(TumorDetails(hasLiverLesions = true))
        assertThat(lesions).isEqualTo("Liver")
    }

    @Test
    fun `Should return string for lung lesions`() {
        val lesions = lesions(TumorDetails(hasLungLesions = true))
        assertThat(lesions).isEqualTo("Lung")
    }

    @Test
    fun `Should concatenate multiple lesions together`() {
        val lesions = lesions(TumorDetails(hasLiverLesions = true, hasLungLesions = true))
        assertThat(lesions).isEqualTo("Liver, Lung")
    }

    @Test
    fun `Should return string for lymph node lesions`() {
        val lesions = lesions(TumorDetails(otherLesions = listOf("lymph node first", "lymph node second")))
        assertThat(lesions).isEqualTo("Lymph nodes (first, second)")
    }

    @Test
    fun `Should return lymph nodes when input is lymph nodes without location specification`() {
        val lesions = lesions(TumorDetails(otherLesions = listOf("lymph nodes")))
        assertThat(lesions).isEqualTo("Lymph nodes")
    }
}