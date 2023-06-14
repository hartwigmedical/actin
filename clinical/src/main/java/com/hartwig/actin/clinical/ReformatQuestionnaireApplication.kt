package com.hartwig.actin.clinical

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.system.exitProcess

class ReformatQuestionnaireApplication private constructor(private val questionnaireFile: String) {
    @Throws(IOException::class)
    fun run() {
        val questionnaire = java.lang.String.join("\\n", Files.readAllLines(File(questionnaireFile).toPath()))
        println(questionnaire)
    }

    companion object {
        private val LOGGER = LogManager.getLogger(
            ReformatQuestionnaireApplication::class.java
        )
        private const val QUESTIONNAIRE = "questionnaire"
        private const val APPLICATION = "ACTIN Questionnaire Reformat"

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val options = Options()
            options.addOption(QUESTIONNAIRE, true, "File containing the questionnaire txt")
            val questionnaireFile: String
            try {
                val cmd = DefaultParser().parse(options, args)
                questionnaireFile = ApplicationConfig.nonOptionalFile(cmd, QUESTIONNAIRE)
            } catch (exception: ParseException) {
                LOGGER.warn(exception)
                HelpFormatter().printHelp(APPLICATION, options)
                exitProcess(1)
            }
            ReformatQuestionnaireApplication(questionnaireFile).run()
        }
    }
}