package com.hartwig.actin.clinical

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess

class ReformatQuestionnaireApplication(private val questionnaireFile: String) {
    @Throws(IOException::class)
    fun run() {
        val parts = Files.readAllLines(File(questionnaireFile).toPath()).joinToString("\\n").split("\\n", limit = 2)
        val questionnaire =
            listOf(
                parts[0],
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")),
                "consultation",
                "Beloop",
                parts[1]
            ).joinToString("\"\t\"", "\"", "\"")
        println(questionnaire)
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(ReformatQuestionnaireApplication::class.java)
        const val QUESTIONNAIRE = "questionnaire"
        const val APPLICATION = "ACTIN Questionnaire Reformat"
    }
}

@Throws(IOException::class)
fun main(args: Array<String>) {
    val options = Options()
    options.addOption(ReformatQuestionnaireApplication.QUESTIONNAIRE, true, "File containing the questionnaire txt")
    val questionnaireFile: String
    try {
        val cmd = DefaultParser().parse(options, args)
        questionnaireFile = ApplicationConfig.nonOptionalFile(cmd, ReformatQuestionnaireApplication.QUESTIONNAIRE)
    } catch (exception: ParseException) {
        ReformatQuestionnaireApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(ReformatQuestionnaireApplication.APPLICATION, options)
        exitProcess(1)
    }
    ReformatQuestionnaireApplication(questionnaireFile).run()
}
