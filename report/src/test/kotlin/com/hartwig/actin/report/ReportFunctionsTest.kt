package com.hartwig.actin.report

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.ReportFunctions.lesions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReportFunctionsTest {

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
}