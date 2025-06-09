package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.PriorPrimaryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TumorStatus
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.feed.datamodel.DatedEntry
import com.hartwig.feed.datamodel.FeedPatientDetail
import com.hartwig.feed.datamodel.FeedPatientRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

private const val SECOND_PRIMARY_INPUT = "Second primary input"

private const val TUMOR_LOCATION_INTERPRETATION = "Tumor location interpretation"

private const val TREATMENT_HISTORY_INPUT = "Treatment history input"

class PriorPrimaryExtractorTest {
    private val extractor = PriorPrimaryExtractor(
        TestCurationFactory.curationDatabase(
            PriorPrimaryConfig(
                input = SECOND_PRIMARY_INPUT,
                ignore = false,
                curated = PriorPrimary(
                    status = TumorStatus.ACTIVE,
                    tumorType = "tumorType",
                    tumorSubType = "tumorSubType",
                    treatmentHistory = "treatmentHistory",
                    tumorLocation = TUMOR_LOCATION_INTERPRETATION,
                    tumorSubLocation = "tumorSubLocation"
                )
            )
        ),
        TestCurationFactory.curationDatabase(
            TreatmentHistoryEntryConfig(
                input = TREATMENT_HISTORY_INPUT, ignore = false, curated = TreatmentTestFactory.treatmentHistoryEntry()
            )
        )
    )

    private val feedRecord = FeedPatientRecord(
        patientDetails =
            FeedPatientDetail(1950, "Male", registrationDate = LocalDate.of(2025, 5, 5), "ACTIN001010001")
    )

    @Test
    fun `Should curate prior second primaries from second primary config`() {
        val inputs = listOf(SECOND_PRIMARY_INPUT, CANNOT_CURATE).map { DatedEntry(it, null) }
        val (priorPrimaries, evaluation) = extractor.extract(PATIENT_ID, feedRecord.copy(priorPrimaries = inputs))
        assertThat(priorPrimaries).hasSize(1)
        assertThat(priorPrimaries[0].tumorLocation).isEqualTo(TUMOR_LOCATION_INTERPRETATION)

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.PRIOR_PRIMARY,
                CANNOT_CURATE,
                "Could not find second primary or treatment history config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.priorPrimaryEvaluatedInputs).isEqualTo(inputs.map { it.name.lowercase() }.toSet())
    }

    @Test
    fun `Should suppress warnings when prior second primaries from treatment history`() {
        val inputs = listOf(TREATMENT_HISTORY_INPUT).map { DatedEntry(it, null) }
        val (_, evaluation) = extractor.extract(PATIENT_ID, feedRecord.copy(priorPrimaries = inputs))
        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract empty list when questionnaire second primary list is null`() {
        assertThat(extractor.extract(PATIENT_ID, feedRecord).extracted).isEmpty()
    }
}