package com.hartwig.actin.algo

import com.hartwig.actin.datamodel.trial.EligibilityRuleDefinition
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.util.json.ActinObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import io.github.oshai.kotlinlogging.KotlinLogging

class EligibilityRuleExportApplication(private val config: EligibilityRuleExportConfig) {

    fun run() {
        logger.info { "Running $APPLICATION v$VERSION" }

        val outputFile = Path.of(config.outputDirectory).resolve(OUTPUT_FILE_NAME)
        val rules = EligibilityRule.entries.map { rule ->
            EligibilityRuleDefinition(rule = rule.name, parameters = rule.input)
        }

        logger.info { "Writing ${rules.size} eligibility rules to $outputFile" }
        Files.newBufferedWriter(outputFile).use { writer ->
            writer.write(mapper.writeValueAsString(rules))
        }
        logger.info { "Done!" }
    }

    companion object {
        const val APPLICATION = "ACTIN Eligibility Rule Export"
        private const val OUTPUT_FILE_NAME = "eligibility_rules.json"

        val logger = KotlinLogging.logger {}
        private val VERSION =
            EligibilityRuleExportApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
        private val mapper by lazy { ActinObjectMapper.create() }
    }
}

fun main(args: Array<String>) {
    val options: Options = EligibilityRuleExportConfig.createOptions()
    val config: EligibilityRuleExportConfig
    try {
        config = EligibilityRuleExportConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        EligibilityRuleExportApplication.logger.error(exception) { exception.message ?: "" }
        HelpFormatter().printHelp(EligibilityRuleExportApplication.APPLICATION, options)
        exitProcess(1)
    }

    EligibilityRuleExportApplication(config).run()
}
