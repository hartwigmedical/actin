package com.hartwig.actin.clinical

import com.google.common.io.Resources
import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.feed.FEED_DIRECTORY
import com.hartwig.actin.clinical.feed.FeedModel
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

val EXPECTED_CLINICAL_RECORD: String =
    "${Resources.getResource("clinical_record").path}/ACTN01029999.clinical.json"

class ClinicalIngestionTest {

    @Test
    fun `Should run ingestion from proper curation and feed files, read from filesystem`() {
        val ingestion = ClinicalIngestion.create(
            FeedModel.fromFeedDirectory(FEED_DIRECTORY),
            CurationDatabaseContext.create(
                CURATION_DIRECTORY,
                CurationDoidValidator(TestDoidModelFactory.createMinimalTestDoidModel()),
                TestTreatmentDatabaseFactory.createProper()
            ),
            TestAtcFactory.createProperAtcModel()
        )

        val ingestionResult = ingestion.run()
        assertThat(ingestionResult).isNotNull
        val patientResults = ingestionResult.patientResults
        assertThat(patientResults[0].status).isEqualTo(PatientIngestionStatus.PASS)
        assertThat(patientResults).hasSize(1)
        assertThat(patientResults[0].patientId).isEqualTo("ACTN01029999")
        assertThat(patientResults[0].curationResults).isEmpty()
        assertThat(patientResults[0].clinicalRecord).isEqualTo(ClinicalRecordJson.read(EXPECTED_CLINICAL_RECORD))
    }
}