package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.google.common.collect.Lists
import java.util.StringJoiner

internal object QuestionnaireReader {

    val TERMS_TO_CLEAN =
        setOf("{", "}", "\\tab", "\\li0", "\\ri0", "\\sa0", "\\sb0", "\\u000ci0", "\\ql", "\\par", "\\u000c2", "\\ltrch", "\\fi0", "\\f2")

    fun read(entryText: String, validKeys: List<String>): Array<String> {
        return merge(clean(entryText, validKeys).split("\\n").dropLastWhile { it.isEmpty() }.toTypedArray(), validKeys)
    }

    private fun merge(lines: Array<String>, validKeys: List<String>): Array<String> {
        val merged: MutableList<String> = Lists.newArrayList()
        var curLine = newValueStringJoiner()
        for (i in lines.indices) {
            curLine.add(lines[i])
            if (lines[i].trim { it <= ' ' }.isEmpty() || i == lines.size - 1 || isField(
                    lines[i + 1],
                    validKeys
                ) || lines[i + 1].trim { it <= ' ' }.isEmpty()
            ) {
                merged.add(curLine.toString())
                curLine = newValueStringJoiner()
            }
        }
        return merged.toTypedArray()
    }

    private fun isField(line: String, validKeys: List<String>): Boolean {
        return line.contains(QuestionnaireExtraction.KEY_VALUE_SEPARATOR) && validKeys.any { line.contains(it) }
    }

    private fun newValueStringJoiner(): StringJoiner {
        return StringJoiner(QuestionnaireExtraction.VALUE_LIST_SEPARATOR_1)
    }

    private fun clean(entryText: String, validKeys: List<String>): String {
        val cleanedText = TERMS_TO_CLEAN.fold(entryText) { acc, term ->
            acc.replace(term, "")
        }
        val pattern = "(${validKeys.joinToString("|")}):\\s*((\\\\n){2,})".toRegex()
        return pattern.replace(cleanedText) { match ->
            "${match.groupValues[1]}:\\n"
        }
    }
}

