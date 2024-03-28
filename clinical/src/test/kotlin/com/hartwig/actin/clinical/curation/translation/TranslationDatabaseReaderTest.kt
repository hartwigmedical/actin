package com.hartwig.actin.clinical.curation.translation

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TranslationDatabaseReaderTest {

    @Test
    fun `Should read translation database from TSV`() {
        assertThat(
            TranslationDatabaseReader.read(
                CURATION_DIRECTORY,
                TranslationDatabaseReader.ADMINISTRATION_ROUTE_TRANSLATION_TSV,
                AdministrationRouteTranslationFactory(),
                CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION
            ) { emptySet() }.find("ORAAL")!!.translated
        ).isEqualTo("Oral")
    }

    companion object {
        private val CURATION_DIRECTORY = resourceOnClasspath("curation/")
    }
}