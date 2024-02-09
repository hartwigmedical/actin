package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.google.common.collect.Lists
import org.apache.logging.log4j.util.Strings
import java.util.StringJoiner

internal object QuestionnaireReader {
    val TERMS_TO_CLEAN = setOf("{", "}", "\\tab", "\\li0", "\\ri0", "\\sa0", "\\sb0", "\\u000ci0", "\\ql", "\\par", "\\u000c2", "\\ltrch")
    fun read(entryText: String, validKeys: List<String>): Array<String> {
        return merge(clean(entryText).split("\\n").dropLastWhile { it.isEmpty() }.toTypedArray(), validKeys)
    }

    private fun merge(lines: Array<String>, validKeys: List<String>): Array<String> {
        val merged: MutableList<String> = Lists.newArrayList()
        var curLine = newValueStringJoiner()
        for (i in lines.indices) {
            curLine.add(lines[i])
            if (lines[i].trim { it <= ' ' }.isEmpty() || i == lines.size - 1 || isField(
                    lines[i + 1],
                    validKeys
                ) || lines[i + 1].trim { it <= ' ' }
                    .isEmpty()
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

    private fun clean(entryText: String): String {
        var cleanText = entryText
        for (str in TERMS_TO_CLEAN) {
            cleanText = cleanText.replace(str, Strings.EMPTY)
        }
        return cleanText
    }
}