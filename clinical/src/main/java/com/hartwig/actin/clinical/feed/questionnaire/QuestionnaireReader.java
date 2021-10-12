package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class QuestionnaireReader {

    @VisibleForTesting
    static final Set<String> TERMS_TO_CLEAN = Sets.newHashSet();

    static {
        TERMS_TO_CLEAN.add("{");
        TERMS_TO_CLEAN.add("}");

        TERMS_TO_CLEAN.add("\\tab");
        TERMS_TO_CLEAN.add("\\li0");
        TERMS_TO_CLEAN.add("\\ri0");
        TERMS_TO_CLEAN.add("\\sa0");
        TERMS_TO_CLEAN.add("\\sb0");
        TERMS_TO_CLEAN.add("\\fi0");
        TERMS_TO_CLEAN.add("\\ql");
        TERMS_TO_CLEAN.add("\\par");
        TERMS_TO_CLEAN.add("\\f2");
        TERMS_TO_CLEAN.add("\\ltrch");
    }

    private QuestionnaireReader() {
    }

    @NotNull
    public static String[] read(@NotNull QuestionnaireEntry entry) {
        String[] lines = entry.itemAnswerValueValueString().split("\n");

        return merge(clean(lines));
    }

    @NotNull
    private static String[] merge(@NotNull String[] lines) {
        List<String> merged = Lists.newArrayList();

        StringBuilder curLine = new StringBuilder(" ");
        for (int i = 0; i < lines.length; i++) {
            if (!(i == 0 || hasValue(lines[i - 1]) && !hasValue(lines[i]) && (i == lines.length - 1 || hasValue(lines[i + 1])))) {
                merged.add(curLine.toString().trim());
                curLine = new StringBuilder(" ");
            }
            curLine.append(lines[i]);
        }
        merged.add(curLine.toString().trim());

        String[] mergedLines = new String[merged.size()];
        for (int j = 0; j < merged.size(); j++) {
            mergedLines[j] = merged.get(j);
        }
        return mergedLines;
    }

    private static boolean hasValue(@NotNull String line) {
        return line.contains(QuestionnaireExtraction.KEY_VALUE_SEPARATOR);
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
