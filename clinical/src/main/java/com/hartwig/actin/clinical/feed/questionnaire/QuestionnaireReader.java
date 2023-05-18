package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;

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
}
