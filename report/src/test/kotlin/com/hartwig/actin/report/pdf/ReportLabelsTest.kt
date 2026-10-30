package com.hartwig.actin.report.pdf

import java.io.File
import java.util.Properties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ReportLabelsTest {

    @Test
    fun `Should load labels from resource file`() {
        val labels = ReportLabels.load()
        assertThat(labels.report.title()).isEqualTo("ACTIN Report (research use only)")
    }

    @Test
    fun `Should resolve apostrophe escaping`() {
        val labels = ReportLabels.load()
        assertThat(labels.trialMatching.footnoteChildrensHospital("2 trials"))
            .isEqualTo("2 trials filtered because trial is running exclusively in children's hospital.")
    }

    @Test
    fun `Should substitute parameters`() {
        val labels = ReportLabels.load()
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

    @Test
    fun `Should fall back to the bundled default for keys absent from a partial labels override file`(@TempDir tempDir: File) {
        val overrideFile = File(tempDir, "partial.properties").apply {
            writeText("report.title=Overridden Title\n")
        }

        val labels = ReportLabels.load(listOf(overrideFile.path))

        assertThat(labels.report.title()).isEqualTo("Overridden Title")
        assertThat(labels.misc.notAvailable()).isEqualTo(ReportLabels.load().misc.notAvailable())
    }

    @Test
    fun `Should apply labels override files in order so later files win on conflicting keys`(@TempDir tempDir: File) {
        val firstOverride = File(tempDir, "first.properties").apply {
            writeText("report.title=First Title\n")
        }
        val secondOverride = File(tempDir, "second.properties").apply {
            writeText("report.title=Second Title\n")
        }

        val labels = ReportLabels.load(listOf(firstOverride.path, secondOverride.path))

        assertThat(labels.report.title()).isEqualTo("Second Title")
    }
}
