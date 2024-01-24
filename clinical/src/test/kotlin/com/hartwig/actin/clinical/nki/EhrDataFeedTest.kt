package com.hartwig.actin.clinical.nki

import com.google.common.io.Resources
import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.ImmutableDoidManualConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

val INPUT_JSON: String = Resources.getResource("nki-feed").path

class EhrDataFeedTest {

    @Test
    fun `Should load EHR data from json and convert to clinical record`() {
        val curationDatabase = CurationDatabaseContext.create(
            CURATION_DIRECTORY,
            CurationDoidValidator(
                TestDoidModelFactory.createWithDoidManualConfig(
                    ImmutableDoidManualConfig.builder().build()
                )
            ),
            TestTreatmentDatabaseFactory.createProper()
        )
        val feed = EhrDataFeed(
            INPUT_JSON,
            curationDatabase.qtProlongingCuration,
            curationDatabase.cypInteractionCuration,
            TestTreatmentDatabaseFactory.createProper(),
            TestAtcFactory.createProperAtcModel()
        )

        assertThat(feed.ingest()).isNotNull
    }

}