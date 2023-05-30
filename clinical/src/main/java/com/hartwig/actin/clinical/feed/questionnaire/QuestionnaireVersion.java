package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

enum QuestionnaireVersion {
    V1_6("ACTIN Questionnaire V1.6", Sets.newHashSet()),
    V1_5("ACTIN Questionnaire V1.5", Sets.newHashSet()),
    V1_4("ACTIN Questionnaire V1.4", Sets.newHashSet()),
    V1_3("CNS lesions:", Sets.newHashSet("ACTIN Questionnaire V1.4", "ACTIN Questionnaire V1.5", "ACTIN Questionnaire V1.6")),
    V1_2("-Active:", Sets.newHashSet("CNS lesions:")),
    V1_1("- Active:", Sets.newHashSet()),
    V1_0("\\li0\\ri0", Sets.newHashSet()),
    V0_2("Other (e.g. pleural effusion)", Sets.newHashSet()),
    V0_1("Other (e.g. Osteoporosis, Pleural effusion)", Sets.newHashSet());

    @NotNull
    private final String specificSearchString;
    @NotNull
    private final Set<String> disallowedSearchStrings;

    QuestionnaireVersion(@NotNull final String specificSearchString, @NotNull final Set<String> disallowedSearchStrings) {
        this.specificSearchString = specificSearchString;
        this.disallowedSearchStrings = disallowedSearchStrings;
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
        String[] lines = entry.text().split("\n");

        boolean match = false;
        for (String line : lines) {
            if (line.contains(specificSearchString)) {
                match = true;
            }

            for (String disallowedSearchString : disallowedSearchStrings) {
                if (line.contains(disallowedSearchString)) {
                    return false;
                }
            }
        }

        return match;
    }
}
