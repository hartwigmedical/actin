package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FeedModelTest {

    @Test
    public void canRetrieveSubjects() {
        FeedModel model = new FeedModel(TestFeedFactory.createTestFeed());

        assertTrue(model.subjects().isEmpty());
    }
}