package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.general.HasWHOStatus.COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import org.junit.Assert;
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

    @Test
    public void shouldReturnEmptyForCategorySearchWhenComplicationsAreNull() {
        PatientRecord record = ComplicationTestFactory.withComplications(null);
        Set<String> filteredComplicationNames = PatternMatcher.findComplicationNamesMatchingCategories(record,
                COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS);
        Assert.assertTrue(filteredComplicationNames.isEmpty());

        Set<String> filteredComplicationCategories = PatternMatcher.findComplicationCategoriesMatchingCategories(record,
                COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS);
        Assert.assertTrue(filteredComplicationCategories.isEmpty());
    }

    @Test
    public void shouldReturnListOfComplicationsMatchingCategorySearchTerms() {
        PatientRecord record = ComplicationTestFactory.withComplications(Arrays.asList(
                ComplicationTestFactory.builder()
                        .name("first matching")
                        .addCategories("X", "Y", "the ascites category", "Pleural Effusions")
                        .build(),
                ComplicationTestFactory.builder()
                        .name("other")
                        .addCategories("X", "Y")
                        .build(),
                ComplicationTestFactory.builder()
                        .name("second matching")
                        .addCategories("chronic pain issues", "nothing")
                        .build()
        ));
        Set<String> filteredComplicationNames = PatternMatcher.findComplicationNamesMatchingCategories(record,
                COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS);
        Assert.assertEquals(2, filteredComplicationNames.size());

        Assert.assertTrue(filteredComplicationNames.contains("first matching"));
        Assert.assertTrue(filteredComplicationNames.contains("second matching"));

        Set<String> filteredComplicationCategories = PatternMatcher.findComplicationCategoriesMatchingCategories(record,
                COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS);
        Assert.assertEquals(3, filteredComplicationCategories.size());

        Assert.assertTrue(filteredComplicationCategories.contains("the ascites category"));
        Assert.assertTrue(filteredComplicationCategories.contains("Pleural Effusions"));
        Assert.assertTrue(filteredComplicationCategories.contains("chronic pain issues"));
    }
}