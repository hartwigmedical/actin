package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigFile
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import java.time.LocalDate

val CURATION_DIRECTORY: String = resourceOnClasspath("curation") + "/"

object TestCurationFactory {

    fun curationHeaders(tsv: String) = CurationConfigFile.readTsv(CURATION_DIRECTORY + tsv).second

    fun emptyQuestionnaire(): Questionnaire {
        return Questionnaire(
            LocalDate.now(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null
        )
    }

    fun <T : CurationConfig> curationDatabase(vararg configs: T) =
        CurationDatabase(configs = configsToMap(configs.toList()), validationErrors = emptyList(), category = CurationCategory.ECG) {
            emptySet()
        }

    private fun <T : CurationConfig> configsToMap(configs: List<T>) =
        configs.groupBy { it.input.lowercase() }.mapValues { it.value.toSet() }
}