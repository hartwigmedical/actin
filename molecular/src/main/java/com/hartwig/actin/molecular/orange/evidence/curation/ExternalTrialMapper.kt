package com.hartwig.actin.molecular.orange.evidence.curation

import org.apache.logging.log4j.LogManager

class ExternalTrialMapper(private val mappings: MutableList<ExternalTrialMapping?>) {
    fun map(externalTrialToMap: String): String {
        for (mapping in mappings) {
            if (mapping.externalTrial() == externalTrialToMap) {
                LOGGER.debug("Mapping external trial '{}' to ACTIN trial '{}'", mapping.externalTrial(), mapping.actinTrial())
                return mapping.actinTrial()
            }
        }
        return externalTrialToMap
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ExternalTrialMapper::class.java)
    }
}
