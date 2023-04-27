package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.complication.PatternMatcher.isMatch
import org.junit.Assert
import org.junit.Test

class PatternMatcherTest {
    @Test
    fun canMatchPatterns() {
        val patterns: MutableSet<List<String>> = mutableSetOf()
        Assert.assertFalse(isMatch("term", patterns))
        patterns.add(listOf("found", "pattern"))
        Assert.assertFalse(isMatch("the pattern is not found here", patterns))
        Assert.assertTrue(isMatch("we found the pattern here", patterns))
        patterns.add(listOf("1", "2", "3", "4"))
        Assert.assertFalse(isMatch("something completely different", patterns))
        Assert.assertTrue(isMatch("we can count 1, 2, 3, 4, 5, 6", patterns))
    }
}