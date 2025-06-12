package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.ingestion.QuestionnaireCurationError
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENTID = "subject"

class TumorStageValidatorTest {

    @Test
    fun `Should curate tumor stage when curation exists`() {
        val curated = TumorStageValidator.validate(PATIENTID, "IIb")
        assertThat(curated.curated).isEqualTo(TumorStage.IIB)
        assertThat(curated.errors).isEmpty()
    }

    @Test
    fun `Should not curate tumor stage and return error when curation does not exist`() {
        val curated = TumorStageValidator.validate(PATIENTID, "Not a stage")
        assertThat(curated.curated).isNull()
        assertThat(curated.errors).containsExactly(
            QuestionnaireCurationError(
                PATIENTID,
                "Unrecognized questionnaire tumor stage: 'Not a stage'"
            )
        )
    }

}