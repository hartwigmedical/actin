package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationValidator
import org.apache.logging.log4j.LogManager

class IntoleranceConfigFactory(private val curationValidator: CurationValidator) : CurationConfigFactory<IntoleranceConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): IntoleranceConfig {
        val input = parts[fields["input"]!!]
        val doids = CurationUtil.toDOIDs(parts[fields["doids"]!!])
        // TODO Should consider how to model "we know for certain this patient has no intolerances".
        if (!input.equals(INTOLERANCE_INPUT_TO_IGNORE_FOR_DOID_CURATION, ignoreCase = true) && !curationValidator.isValidDiseaseDoidSet(
                doids
            )
        ) {
            LOGGER.warn("Intolerance config with input '{}' contains at least one invalid doid: '{}'", input, doids)
        }
        return IntoleranceConfig(input = input, name = parts[fields["name"]!!], doids = doids)
    }

    companion object {
        private val LOGGER = LogManager.getLogger(IntoleranceConfigFactory::class.java)
        private const val INTOLERANCE_INPUT_TO_IGNORE_FOR_DOID_CURATION = "Geen"
    }

}