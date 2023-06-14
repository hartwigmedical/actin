package com.hartwig.actin.clinical.feed

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import com.hartwig.actin.util.TabularFile
import java.io.File
import java.io.IOException
import java.nio.file.Files

class FeedFileReader<T : FeedEntry>(private val feedEntryCreator: FeedEntryCreator<T>, private val expectLineBreaks: Boolean) {
    @Throws(IOException::class)
    fun read(feedTsv: String): List<T> {
        val lines = Files.readAllLines(File(feedTsv).toPath())
        val fields = TabularFile.createFields(splitFeedLine(lines[0]))
        val entries: MutableList<T> = Lists.newArrayList()
        if (lines.size > 1) {
            var curLine = StringBuilder(lines[1])
            for (line in lines.subList(2, lines.size)) {
                // Entries appear on multiple lines in case they contain hard line breaks so append to the end.
                if (!hasExpectedFields(line, fields)) {
                    val lineToAppend = if (expectLineBreaks) line.replace(DELIMITER.toRegex(), "") else line
                    if (expectLineBreaks) {
                        curLine.append("\n").append(lineToAppend)
                    } else if (hasExpectedFields(curLine.toString(), fields)) {
                        // Apparently the new unfinished line is the start of a new entry.
                        addToEntries(entries, fields, curLine.toString())
                        curLine = StringBuilder(lineToAppend)
                    } else {
                        // The unfinished new line is part of something that is building up towards a valid entry.
                        curLine.append(lineToAppend)
                    }
                } else if (!hasExpectedFields(curLine.toString(), fields)) {
                    // This should only happen in case an unexpected line break happened in the first column.
                    curLine.append(line)
                } else {
                    addToEntries(entries, fields, curLine.toString())
                    curLine = StringBuilder(line)
                }
            }
            // Need to add the final accumulated entry
            addToEntries(entries, fields, curLine.toString())
        }
        return entries
    }

    private fun addToEntries(entries: MutableList<T>, fields: Map<String, Int>, line: String) {
        val reformatted = fixLineBreaks(line)
        val parts = splitFeedLine(reformatted)
        if (parts.any { it.isNotEmpty() }) {
            val feedLine = FeedLine(fields, parts)
            if (feedEntryCreator.isValid(feedLine)) {
                entries.add(feedEntryCreator.fromLine(feedLine))
            }
        }
    }

    companion object {
        private const val DELIMITER = "\t"
        fun <T : FeedEntry> create(feedEntryCreator: FeedEntryCreator<T>): FeedFileReader<T> {
            return FeedFileReader(feedEntryCreator, false)
        }

        private fun hasExpectedFields(line: String, fields: Map<String, Int>): Boolean {
            return splitFeedLine(line).size == fields.size
        }

        private fun splitFeedLine(line: String): Array<String> {
            return cleanQuotes(line.split(DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
        }

        @JvmStatic
        @VisibleForTesting
        fun cleanQuotes(inputs: Array<String>): Array<String> {
            return inputs.indices.map { cleanQuotes(inputs[it]) }.toTypedArray()
        }

        private fun cleanQuotes(input: String): String {
            val firstQuote = input.indexOf("\"")
            val lastQuote = input.lastIndexOf("\"")
            val cleaned = if (firstQuote >= 0 && lastQuote >= 0 && lastQuote > firstQuote) {
                input.substring(firstQuote + 1, lastQuote)
            } else input

            // Replace all double quotes with single quotes.
            return cleaned.replace("\"\"".toRegex(), "\"")
        }

        @VisibleForTesting
        fun fixLineBreaks(input: String): String {
            return input.replace("\\n", "\n")
        }
    }
}