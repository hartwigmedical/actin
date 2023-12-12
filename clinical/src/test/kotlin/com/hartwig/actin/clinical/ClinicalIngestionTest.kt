package com.hartwig.actin.clinical

import com.google.common.io.Resources
import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.feed.FEED_DIRECTORY
import com.hartwig.actin.clinical.feed.FeedModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClinicalIngestionTest {

    @Test
    fun `Should run ingestion from proper curation and feed files, read from filesystem`() {
        val ingestion = ClinicalIngestion.create(
            CURATION_DIRECTORY,
            FeedModel.fromFeedDirectory(FEED_DIRECTORY),
            CurationDoidValidator(
                DoidModelFactory.createFromDoidEntry(
                    DoidJson.readDoidOwlEntry(
                        Resources.getResource("doids/doid.json").path
                    )
                )
            ),
            TestTreatmentDatabaseFactory.createProper(),
            TestAtcFactory.createProperAtcModel()
        )

        val ingestionResult = ingestion.run()
        assertThat(ingestionResult).isNotNull
        assertThat(ingestionResult.curationValidationErrors).isEmpty()
        val patientResults = ingestionResult.patientResults
        assertThat(patientResults[0].status).isEqualTo(PatientIngestionStatus.PASS)
        assertThat(patientResults).hasSize(1)
        assertThat(patientResults[0].patientId).isEqualTo("ACTN01029999")
    }
}