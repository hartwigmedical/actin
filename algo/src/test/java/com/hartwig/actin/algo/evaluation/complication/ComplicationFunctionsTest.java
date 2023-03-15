package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.general.WHOFunctions.COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS;

import java.util.Arrays;
import java.util.Set;

import com.hartwig.actin.PatientRecord;

import org.junit.Assert;
import org.junit.Test;

public class ComplicationFunctionsTest {

    @Test
    public void shouldReturnEmptyForCategorySearchWhenComplicationsAreNull() {
        PatientRecord record = ComplicationTestFactory.withComplications(null);
        Set<String> filteredComplicationNames =
                ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS);
        Assert.assertTrue(filteredComplicationNames.isEmpty());

        Set<String> filteredComplicationCategories =
                ComplicationFunctions.findComplicationCategoriesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS);
        Assert.assertTrue(filteredComplicationCategories.isEmpty());
    }

    @Test
    public void shouldReturnListOfComplicationsMatchingCategorySearchTerms() {
        PatientRecord record = ComplicationTestFactory.withComplications(Arrays.asList(ComplicationTestFactory.builder()
                        .name("first matching")
                        .addCategories("X", "Y", "the ascites category", "Pleural Effusions")
                        .build(),
                ComplicationTestFactory.builder().name("other").addCategories("X", "Y").build(),
                ComplicationTestFactory.builder().name("second matching").addCategories("chronic pain issues", "nothing").build()));
        Set<String> filteredComplicationNames =
                ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS);
        Assert.assertEquals(2, filteredComplicationNames.size());

        Assert.assertTrue(filteredComplicationNames.contains("first matching"));
        Assert.assertTrue(filteredComplicationNames.contains("second matching"));

        Set<String> filteredComplicationCategories =
                ComplicationFunctions.findComplicationCategoriesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS);
        Assert.assertEquals(3, filteredComplicationCategories.size());

        Assert.assertTrue(filteredComplicationCategories.contains("the ascites category"));
        Assert.assertTrue(filteredComplicationCategories.contains("Pleural Effusions"));
        Assert.assertTrue(filteredComplicationCategories.contains("chronic pain issues"));
    }
}
