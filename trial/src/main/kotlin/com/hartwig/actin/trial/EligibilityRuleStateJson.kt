package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.EligibilityRuleState
import com.hartwig.actin.util.Paths
import com.hartwig.actin.util.json.GsonSerializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedWriter
import java.io.FileWriter

object EligibilityRuleStateJson {

    private val LOGGER: Logger = LogManager.getLogger(EligibilityRuleStateJson::class.java)
    private const val TRIAL_INGESTION_JSON_EXTENSION: String = "rule-state-result.json"

    fun write(eligibilityRulesState: Set<EligibilityRuleState>, directory: String) {
        val path: String = Paths.forceTrailingFileSeparator(directory)
        val jsonFile: String = path + TRIAL_INGESTION_JSON_EXTENSION
        LOGGER.info(" Writing {} rules to {}", eligibilityRulesState.size, jsonFile)
        val writer = BufferedWriter(FileWriter(jsonFile))
        writer.write(toJson(eligibilityRulesState))
        writer.close()
    }

    fun toJson(eligibilityRulesState: Set<EligibilityRuleState>): String {
        return GsonSerializer.create().toJson(eligibilityRulesState)
    }

}