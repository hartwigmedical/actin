package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.configuration.ReportIntendedUse
import java.util.Properties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class EvaluationLabelsTest {

    @Test
    fun `Should load messages from resource file`() {
        val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY)
        assertThat(labels.general.isMalePass()).isEqualTo("Patient is male")
    }

    @Test
    fun `Should load both intended use variants`() {
        assertThat(EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).general.isMalePass()).isEqualTo("Patient is male")
        assertThat(EvaluationLabels.load(ReportIntendedUse.NON_MEDICAL).general.isMalePass()).isEqualTo("Patient is male")
    }

    @Test
    fun `Should substitute parameters`() {
        val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY)
        assertThat(labels.general.hasAtLeastCertainAgePass(50)).isEqualTo("Patient is older than 50")
    }

    @Test
    fun `Should throw exception on missing key`() {
        assertThatThrownBy { EvaluationLabels(Properties()).general.isMalePass() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("general.is.male.pass")
    }

    @Test
    fun `Should resolve apostrophe escaping`() {
        val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY)
        assertThat(labels.molecular.geneIsWildTypeWarnNoEffect("BRAF p.V600E", "BRAF", "CKB"))
            .isEqualTo("Reportable event(s) BRAF p.V600E in BRAF - however these are annotated with protein effect 'no effect' in CKB and thus may potentially be considered wild-type")
    }

    @Test
    fun `Should preserve leading space in suffix message`() {
        val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY)
        assertThat(labels.molecular.hasHer2ExpressionByIhcSuffixErbb2Amplified())
            .isEqualTo(" (but ERBB2 amplification detected in DNA)")
    }
}
