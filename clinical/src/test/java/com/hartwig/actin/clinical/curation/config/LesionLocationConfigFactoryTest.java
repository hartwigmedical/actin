package com.hartwig.actin.clinical.curation.config;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory;

import org.junit.Test;

public class LesionLocationConfigFactoryTest {

    @Test
    public void canResolveCategories() {
        assertEquals(LesionLocationCategory.LYMPH_NODE, LesionLocationConfigFactory.toCategory("LYMPH NODE"));
    }
}