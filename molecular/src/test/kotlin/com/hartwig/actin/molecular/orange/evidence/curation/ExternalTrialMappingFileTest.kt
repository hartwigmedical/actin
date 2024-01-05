package com.hartwig.actin.molecular.orange.evidence.curation

import com.google.common.io.Resources
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExternalTrialMappingFileTest {

    @Test
    fun `Should read external trial mapping tsv`() {
        val mappings = ExternalTrialMappingFile.read(EXAMPLE_TSV)

        assertThat(mappings.size.toLong()).isEqualTo(2)
        assertThat(findByExternalTrial(mappings, "TR1").actinTrial).isEqualTo("Trial 1")
        assertThat(findByExternalTrial(mappings, "TR2").actinTrial).isEqualTo("TR2")
    }

    companion object {
        private val EXAMPLE_TSV = Resources.getResource("curation/external_trial_mapping.tsv").path

        private fun findByExternalTrial(mappings: List<ExternalTrialMapping>, externalTrialToFind: String): ExternalTrialMapping {
            return mappings.find { it.externalTrial == externalTrialToFind }
                ?: throw IllegalStateException("Could not find external trial in mapping list: $externalTrialToFind")
        }
    }
}