package com.hartwig.actin.molecular.orange.evidence.curation

import com.google.common.io.Resources
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class ExternalTrialMappingFileTest {
    @Test
    @Throws(IOException::class)
    fun canReadExternalTrialMappingTsv() {
        val mappings = ExternalTrialMappingFile.read(EXAMPLE_TSV)
        Assert.assertEquals(2, mappings.size.toLong())
        Assert.assertEquals("Trial 1", findByExternalTrial(mappings, "TR1").actinTrial)
        Assert.assertEquals("TR2", findByExternalTrial(mappings, "TR2").actinTrial)
    }

    companion object {
        private val EXAMPLE_TSV = Resources.getResource("curation/external_trial_mapping.tsv").path
        private fun findByExternalTrial(mappings: MutableList<ExternalTrialMapping>,
                                        externalTrialToFind: String): ExternalTrialMapping {
            for (mapping in mappings) {
                if (mapping.externalTrial == externalTrialToFind) {
                    return mapping
                }
            }
            throw IllegalStateException("Could not find external trial in mapping list: $externalTrialToFind")
        }
    }
}