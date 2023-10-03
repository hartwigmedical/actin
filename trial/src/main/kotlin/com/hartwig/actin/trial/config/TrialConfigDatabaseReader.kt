package com.hartwig.actin.trial.config

import com.hartwig.actin.trial.FileUtil
import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager

object TrialConfigDatabaseReader {

    private val LOGGER = LogManager.getLogger(TrialConfigDatabaseReader::class.java)

    private const val TRIAL_DEFINITION_TSV = "trial_definition.tsv"
    private const val COHORT_DEFINITION_TSV = "cohort_definition.tsv"
    private const val INCLUSION_CRITERIA_TSV = "inclusion_criteria.tsv"
    private const val INCLUSION_CRITERIA_REFERENCE_TSV = "inclusion_criteria_reference.tsv"

    fun read(trialConfigDirectory: String): TrialConfigDatabase {
        LOGGER.info("Reading trial config from {}", trialConfigDirectory)
        val basePath = Paths.forceTrailingFileSeparator(trialConfigDirectory)
        return TrialConfigDatabase(
            trialDefinitionConfigs = readConfigs(basePath + TRIAL_DEFINITION_TSV, TrialDefinitionConfigFactory()),
            cohortDefinitionConfigs = readConfigs(basePath + COHORT_DEFINITION_TSV, CohortDefinitionConfigFactory()),
            inclusionCriteriaConfigs = readConfigs(basePath + INCLUSION_CRITERIA_TSV, InclusionCriteriaConfigFactory()),
            inclusionCriteriaReferenceConfigs = readConfigs(
                basePath + INCLUSION_CRITERIA_REFERENCE_TSV,
                InclusionCriteriaReferenceConfigFactory()
            )
        )
    }

    private fun <T : TrialConfig> readConfigs(tsv: String, factory: TrialConfigFactory<T>): List<T> {
        val configs = FileUtil.createObjectsFromTsv(tsv, factory::create)
        LOGGER.info(" Read {} configs from {}", configs.size, tsv)
        return configs
    }
}