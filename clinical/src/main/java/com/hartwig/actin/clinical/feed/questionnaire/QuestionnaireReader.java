package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class QuestionnaireReader {

    @VisibleForTesting
    static final Set<String> TERMS_TO_CLEAN = Set.of("{",
            "}",
            "\\tab",
            "\\li0",
            "\\ri0",
            "\\sa0",
            "\\sb0",
            "\\fi0",
            "\\ql",
            "\\par",
            "\\f2",
            "\\ltrch",
            "Tumor details",
            "Clinical details",
            "Clinical information");

    private QuestionnaireReader() {
    }

    @NotNull
    public static String cleanedContents(@NotNull QuestionnaireEntry entry) {
        String contents = entry.itemAnswerValueValueString();
        for (String str : TERMS_TO_CLEAN) {
            contents = contents.replace(str, Strings.EMPTY);
        }
        return contents;
    }

    @NotNull
    public static String[] read(@NotNull QuestionnaireEntry entry) {
        String[] lines = entry.itemAnswerValueValueString().split("\n");

        return merge(clean(lines));
    }

    @NotNull
    private static String[] merge(@NotNull String[] lines) {
        List<String> merged = Lists.newArrayList();

        StringJoiner curLine = newValueStringJoiner();
        for (int i = 0; i < lines.length; i++) {
            if (!(i == 0 || hasValue(lines[i - 1]) && !hasValue(lines[i]) && (i == lines.length - 1 || hasValue(lines[i + 1])))) {
                merged.add(curLine.toString());
                curLine = newValueStringJoiner();
            }
            curLine.add(lines[i]);
        }
        merged.add(curLine.toString());
        return merged.toArray(new String[0]);
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
