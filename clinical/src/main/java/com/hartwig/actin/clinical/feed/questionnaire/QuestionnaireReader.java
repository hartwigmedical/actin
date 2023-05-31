package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Lists;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class QuestionnaireReader {

    static final Set<String> TERMS_TO_CLEAN =
            Set.of("{", "}", "\\tab", "\\li0", "\\ri0", "\\sa0", "\\sb0", "\\fi0", "\\ql", "\\par", "\\f2", "\\ltrch");

    private QuestionnaireReader() {
    }

    @NotNull
    public static String[] read(@NotNull QuestionnaireEntry entry) {
        return merge(clean(entry.itemAnswerValueValueString()).split("\n"));
    }

    @NotNull
    private static String[] merge(@NotNull String[] lines) {
        List<String> merged = Lists.newArrayList();

        StringJoiner curLine = newValueStringJoiner();
        for (int i = 0; i < lines.length; i++) {
            curLine.add(lines[i]);
            if (lines[i].isEmpty() || i == lines.length - 1 || isField(lines[i + 1]) || lines[i + 1].isEmpty()) {
                merged.add(curLine.toString());
                curLine = newValueStringJoiner();
            }
        }

        return merged.toArray(new String[0]);
    }

    private static boolean isField(@NotNull String line) {
        return line.contains(QuestionnaireExtraction.KEY_VALUE_SEPARATOR);
    }

    @NotNull
    private static StringJoiner newValueStringJoiner() {
        return new StringJoiner(QuestionnaireExtraction.VALUE_LIST_SEPARATOR_1);
    }

    @NotNull
    private static String clean(@NotNull String entryText) {
        String cleanText = entryText;
        for (String str : TERMS_TO_CLEAN) {
            cleanText = cleanText.replace(str, Strings.EMPTY);
        }
        return cleanText;
    }
}
