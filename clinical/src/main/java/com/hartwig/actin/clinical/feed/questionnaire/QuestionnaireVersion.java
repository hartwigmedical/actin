package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum QuestionnaireVersion {
    V1_3("CNS lesions:", null),
    V1_2("-Active:", "CNS lesions:"),
    V1_1("- Active:", null),
    V1_0("\\li0\\ri0", null),
    V0_2("Other (e.g. pleural effusion)", null),
    V0_1("Other (e.g. Osteoporosis, Pleural effusion)", null);

    @NotNull
    private final String specificSearchString;
    @Nullable
    private final String disallowedSearchString;

    QuestionnaireVersion(@NotNull final String specificSearchString, @Nullable final String disallowedSearchString) {
        this.specificSearchString = specificSearchString;
        this.disallowedSearchString = disallowedSearchString;
    }

    @NotNull
    public static QuestionnaireVersion version(@NotNull QuestionnaireEntry entry) {
        Set<QuestionnaireVersion> matches = Sets.newHashSet();
        for (QuestionnaireVersion version : QuestionnaireVersion.values()) {
            if (version.isMatch(entry)) {
                matches.add(version);
            }
        }

        if (matches.size() > 1) {
            throw new IllegalStateException("Questionnaire for " + entry.subject() + " matched to multiple versions: " + matches);
        } else if (matches.isEmpty()) {
            throw new IllegalStateException("Could not find a match for questionnaire version for " + entry.subject());
        }

        return matches.iterator().next();
    }

    private boolean isMatch(@NotNull QuestionnaireEntry entry) {
        String[] lines = entry.itemAnswerValueValueString().split("\n");

        boolean match = false;
        for (String line : lines) {
            if (line.contains(specificSearchString)) {
                match = true;
            }

            if (disallowedSearchString != null && line.contains(disallowedSearchString)) {
                return false;
            }
        }

        return match;
    }
}
