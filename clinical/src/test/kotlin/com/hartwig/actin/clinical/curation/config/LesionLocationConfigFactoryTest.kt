package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.config.LesionLocationConfigFactory.Companion.toCategory
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import junit.framework.TestCase.assertEquals
import org.junit.Test

class LesionLocationConfigFactoryTest {
    @Test
    fun canResolveCategories() {
        assertEquals(LesionLocationCategory.LYMPH_NODE, toCategory("LYMPH NODE"))
    }
}