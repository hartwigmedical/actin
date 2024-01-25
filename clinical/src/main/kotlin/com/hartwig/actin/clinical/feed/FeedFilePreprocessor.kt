package com.hartwig.actin.clinical.feed

import java.io.File

class FeedFilePreprocessor {

    fun apply(file: File): File {
        val outputFile = File(file.name + ".preprocessed")
        file.bufferedReader().useLines { lines ->
            outputFile.bufferedWriter().use { writer ->
                lines.forEach { line ->
                    val processedLine = removeEnclosedQuotes(line)
                    writer.write(processedLine)
                    writer.newLine()
                }
            }
        }
        return outputFile
    }

    private fun removeEnclosedQuotes(line: String): String {
        val fields = line.split(Regex("""\t(?=(?:[^"]*"[^"]*")*[^"]*$)"""))

        return fields.joinToString("\t") { field ->
            if (field.startsWith("\"") && field.endsWith("\"")) {
                "\"" + field.drop(1).dropLast(1).replace("\"", "") + "\""
            } else {
                field
            }
        }
    }
}