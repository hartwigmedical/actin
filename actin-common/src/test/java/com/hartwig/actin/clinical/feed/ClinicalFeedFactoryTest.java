package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class ClinicalFeedFactoryTest {

    private static final String CLINICAL_FEED_DIRECTORY = Resources.getResource("clinical/feed").getPath();

    @Test
    public void canLoadFeedFromTestDirectory() throws IOException {
        assertNotNull(ClinicalFeedFactory.loadFromClinicalFeedDirectory(CLINICAL_FEED_DIRECTORY));
    }
}