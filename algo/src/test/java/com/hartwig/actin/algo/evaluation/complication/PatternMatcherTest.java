package com.hartwig.actin.algo.evaluation.complication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Test;

public class PatternMatcherTest {

    @Test
    public void canMatchPatterns() {
        Set<List<String>> patterns = Sets.newHashSet();

        assertFalse(PatternMatcher.isMatch("term", patterns));

        patterns.add(Lists.newArrayList("found", "pattern"));
        assertFalse(PatternMatcher.isMatch("the pattern is not found here", patterns));
        assertTrue(PatternMatcher.isMatch("we found the pattern here", patterns));

        patterns.add(Lists.newArrayList("1", "2", "3", "4"));
        assertFalse(PatternMatcher.isMatch("something completely different", patterns));
        assertTrue(PatternMatcher.isMatch("we can count 1, 2, 3, 4, 5, 6", patterns));
    }
}