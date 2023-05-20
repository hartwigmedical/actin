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
        return merge(clean(entry.itemAnswerValueValueString().split("\n")));
    }

    @NotNull
    private static String[] merge(@NotNull String[] lines) {
        List<String> merged = Lists.newArrayList();

        StringJoiner curLine = newValueStringJoiner();
        curLine.add(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            if (hasValue(lines[i]) || !hasValue(lines[i - 1]) || canContinueToNextLine(lines, i)) {
                merged.add(curLine.toString());
                curLine = newValueStringJoiner();
            }
            curLine.add(lines[i]);
        }
        merged.add(curLine.toString());

        return merged.toArray(new String[0]);
    }

    private static boolean canContinueToNextLine(@NotNull String[] lines, int i) {
        return i != lines.length - 1 && !hasValue(lines[i + 1]);
    }

    private static boolean hasValue(@NotNull String line) {
        return line.contains(QuestionnaireExtraction.KEY_VALUE_SEPARATOR);
    }

    @NotNull
    private static StringJoiner newValueStringJoiner() {
        return new StringJoiner(QuestionnaireExtraction.VALUE_LIST_SEPARATOR_1);
    }

    @NotNull
    private static String[] clean(@NotNull String[] lines) {
        String[] cleaned = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            String clean = lines[i];
            for (String term : TERMS_TO_CLEAN) {
                while (clean.contains(term)) {
                    clean = clean.replace(term, Strings.EMPTY);
                }
            }
            cleaned[i] = clean;
        }
        return cleaned;
    }
}
