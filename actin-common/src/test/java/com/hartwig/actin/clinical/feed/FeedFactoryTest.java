package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class FeedFactoryTest {

    private static final String CLINICAL_DATA_DIRECTORY = Resources.getResource("clinical").getPath();

    @Test
    public void canLoadFeedFromTestDirectory() throws IOException {
        assertNotNull(FeedFactory.loadFromClinicalDataDirectory(CLINICAL_DATA_DIRECTORY));
    }
}