package com.hartwig.actin.report.pdf

import com.hartwig.actin.configuration.ReportIntendedUse
import java.util.Properties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ReportLabelsTest {

    @Test
    fun `Should load labels from resource file`() {
        val labels = ReportLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY)
        assertThat(labels.report.title()).isEqualTo("ACTIN Report (research use only)")
    }

    @Test
    fun `Should resolve apostrophe escaping`() {
        val labels = ReportLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY)
        assertThat(labels.trialMatching.footnoteChildrensHospital("2 trials"))
            .isEqualTo("2 trials filtered because trial is running exclusively in children's hospital.")
    }

    @Test
    fun `Should substitute parameters`() {
        val labels = ReportLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY)
        assertThat(labels.footer.ctgovDisclaimer("2024-01-01"))
            .startsWith("Trials marked with asterisk (*) were sourced from ClinicalTrials.gov on 2024-01-01.")
    }

    @Test
    fun `Should support non-ASCII characters`() {
        val props = Properties().apply { setProperty("misc.not.available", "Niet beschikbäar") }
        assertThat(ReportLabels(props).misc.notAvailable()).isEqualTo("Niet beschikbäar")
    }

    @Test
    fun `Should throw exception on missing key`() {
        assertThatThrownBy { ReportLabels(Properties()).report.title() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("report.title")
    }
}
