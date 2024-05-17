package com.hartwig.actin.trial.config

import com.hartwig.actin.trial.FileUtil
import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files

object TrialConfigDatabaseReader {

    private val LOGGER = LogManager.getLogger(TrialConfigDatabaseReader::class.java)

    private const val TRIAL_DEFINITION_TSV = "trial_definition.tsv"
    private const val COHORT_DEFINITION_TSV = "cohort_definition.tsv"
    private const val INCLUSION_CRITERIA_TSV = "inclusion_criteria.tsv"
    private const val INCLUSION_CRITERIA_REFERENCE_TSV = "inclusion_criteria_reference.tsv"
    private const val UNUSED_RULES_TO_KEEP_TSV = "unused_rules_to_keep.tsv"


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
            ),
            unusedRulesToKeep = readUnusedRulesToKeep(basePath + UNUSED_RULES_TO_KEEP_TSV)
        )
    }

    private fun <T : TrialConfig> readConfigs(tsv: String, factory: TrialConfigFactory<T>): List<T> {
        val configs = FileUtil.createObjectsFromTsv(tsv, factory::create)
        LOGGER.info(" Read {} configs from {}", configs.size, tsv)
        return configs
    }

    private fun readUnusedRulesToKeep(tsv: String): List<String> {
        val allLines = Files.readAllLines(File(tsv).toPath())
        val unusedRulesToKeep = allLines.subList(1, allLines.size)
        LOGGER.info(" Read {} unused rules to keep from {}", unusedRulesToKeep.size, tsv)
        return unusedRulesToKeep
    }
}