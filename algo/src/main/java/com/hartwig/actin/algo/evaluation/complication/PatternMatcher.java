package com.hartwig.actin.algo.evaluation.complication;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

final class PatternMatcher {

    private PatternMatcher() {
    }

    public static boolean isMatch(@NotNull String term, @NotNull Set<List<String>> patterns) {
        String termToEvaluate = term.toLowerCase();
        for (List<String> pattern : patterns) {
            boolean patternMatch = true;
            int prevIndexOf = -1;
            for (String item : pattern) {
                int curIndexOf = termToEvaluate.indexOf(item);
                if (curIndexOf <= prevIndexOf) {
                    patternMatch = false;
                }
                prevIndexOf = curIndexOf;
            }

            if (patternMatch) {
                return true;
            }
        }

        return false;
    }
}
