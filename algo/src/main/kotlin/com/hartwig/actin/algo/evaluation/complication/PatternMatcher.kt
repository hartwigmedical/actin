package com.hartwig.actin.algo.evaluation.complication

internal object PatternMatcher {

    fun isMatch(term: String, patterns: Set<List<String>>): Boolean {
        val termToEvaluate = term.lowercase()
        for (pattern in patterns) {
            var patternMatch = true
            var prevIndexOf = -1
            for (item in pattern) {
                val curIndexOf = termToEvaluate.indexOf(item)
                if (curIndexOf <= prevIndexOf) {
                    patternMatch = false
                }
                prevIndexOf = curIndexOf
            }
            if (patternMatch) {
                return true
            }
        }
        return false
    }
}