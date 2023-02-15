package com.hartwig.actin.algo.evaluation.complication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.clinical.datamodel.Complication;
import org.junit.Test;
import org.junit.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ComplicationUtilTest {
    private final List<String> categorySearchTerms = Arrays.asList(
            "Ascites", "Pleural effusion", "Pericardial effusion", "Pain", "Spinal cord compression"
    );

    @Test
    public void shouldReturnEmptyWhenComplicationsAreNull() {
        ComplicationUtil util = new ComplicationUtil(ComplicationTestFactory.withComplications(null));
        Optional<List<Complication>> filteredComplications = util.complicationsMatchingCategories(categorySearchTerms);
        Assert.assertTrue(filteredComplications.isEmpty());
    }

    @Test
    public void shouldReturnListOfComplicationsMatchingCategorySearchTerms() {
        PatientRecord record = ComplicationTestFactory.withComplications(Arrays.asList(
                ComplicationTestFactory.builder().name("first matching")
                        .addCategories("X", "Y", "the ascites category", "Pleural Effusions").build(),
                ComplicationTestFactory.builder().name("other")
                        .addCategories("X", "Y").build(),
                ComplicationTestFactory.builder().name("second matching")
                        .addCategories("chronic pain issues", "nothing").build()
        ));
        ComplicationUtil util = new ComplicationUtil(record);
        List<Complication> filteredComplications = util.complicationsMatchingCategories(categorySearchTerms)
                .orElseThrow();
        Assert.assertEquals(2, filteredComplications.size());

        Complication firstMatch = filteredComplications.get(0);
        Assert.assertEquals(2, firstMatch.categories().size());
        Assert.assertTrue(firstMatch.categories().contains("the ascites category"));
        Assert.assertTrue(firstMatch.categories().contains("Pleural Effusions"));

        Complication secondMatch = filteredComplications.get(1);
        Assert.assertEquals(1, secondMatch.categories().size());
        Assert.assertTrue(secondMatch.categories().contains("chronic pain issues"));
    }
}