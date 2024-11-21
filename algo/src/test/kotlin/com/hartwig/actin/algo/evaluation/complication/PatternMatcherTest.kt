package com.hartwig.actin.algo.evaluation.complication

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PatternMatcherTest {

    @Test
    fun `Should match all patterns`() {
        val patterns: MutableSet<List<String>> = mutableSetOf()
        assertThat(PatternMatcher.isMatch("term", patterns)).isFalse()

        patterns.add(listOf("found", "pattern"))
        assertThat(PatternMatcher.isMatch("the pattern is not found here", patterns)).isFalse()
        assertThat(PatternMatcher.isMatch("we found the pattern here", patterns)).isTrue()

        patterns.add(listOf("1", "2", "3", "4"))
        assertThat(PatternMatcher.isMatch("something completely different", patterns)).isFalse()
        assertThat(PatternMatcher.isMatch("we can count 1, 2, 3, 4, 5, 6", patterns)).isTrue()
    }
}