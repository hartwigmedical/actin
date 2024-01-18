package com.hartwig.actin.clinical.feed

import java.io.File

class FeedFilePreprocessor {

    fun apply(file: File): File {
        val outputFile = File(file.name + ".escaped")
        file.bufferedReader().useLines { lines ->
            outputFile.bufferedWriter().use { writer ->
                lines.forEach { line ->
                    val processedLine = processLine(line)
                    writer.write(processedLine)
                    writer.newLine()
                }
            }
        }
        return outputFile
    }

    private fun processLine(line: String): String {
        // Split the line by tab while considering quoted fields
        val fields = line.split(Regex("""\t(?=(?:[^"]*"[^"]*")*[^"]*$)"""))

        return fields.joinToString("\t") { field ->
            // Replace internal quotes with escaped quotes
            if (field.startsWith("\"") && field.endsWith("\"")) {
                "\"" + field.drop(1).dropLast(1).replace("\"", "\\\"") + "\""
            } else {
                field
            }
        }
    }
}