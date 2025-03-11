package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toEcg
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toInfectionStatus
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toBoolean
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toSecondaryPrimaries
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toStage
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toWHO
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.ingestion.QuestionnaireCurationError
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val SUBJECT = "subject"

class QuestionnaireCurationTest {

    @Test
    fun `Should curate options when curation exists`() {
        val curated = toBoolean(SUBJECT, "YES")
        assertThat(curated.curated).isTrue
        assertThat(curated.errors).isEmpty()
    }

    @Test
    fun `Should not curate options and return error when curation does not exist`() {
        val curated = toBoolean(SUBJECT, "Not an option")
        assertThat(curated.curated).isNull()
        assertThat(curated.errors).containsExactly(
            QuestionnaireCurationError(
                SUBJECT,
                "Unrecognized questionnaire option: 'Not an option'"
            )
        )
    }

    @Test
    fun `Should curate options without taking into account the case`() {
        assertExpectedExtraction("SusPectEd", null)
        assertExpectedExtraction("yEs", true)
        assertExpectedExtraction("botaantasting BIJ weke DELEN massa", false)
    }

    private fun assertExpectedExtraction(input: String, expected: Boolean?) {
        val (curated, errors) = toBoolean(SUBJECT, input)
        assertThat(curated).isEqualTo(expected)
        assertThat(errors).isEmpty()
    }

    @Test
    fun `Should curate tumor stage when curation exists`() {
        val curated = toStage(SUBJECT, "IIb")
        assertThat(curated.curated).isEqualTo(TumorStage.IIB)
        assertThat(curated.errors).isEmpty()
    }

    @Test
    fun `Should not curate tumor stage and return error when curation does not exist`() {
        val curated = toStage(SUBJECT, "Not a stage")
        assertThat(curated.curated).isNull()
        assertThat(curated.errors).containsExactly(
            QuestionnaireCurationError(
                SUBJECT,
                "Unrecognized questionnaire tumor stage: 'Not a stage'"
            )
        )
    }

    @Test
    fun `Should curate WHO status when curation exists`() {
        val curated = toWHO(SUBJECT, "1")
        assertThat(curated.curated).isEqualTo(1)
        assertThat(curated.errors).isEmpty()
    }

    @Test
    fun `Should not curate WHO status and return error when not between zero and five`() {
        val curated = toWHO(SUBJECT, "7")
        assertThat(curated.curated).isEqualTo(null)
        assertThat(curated.errors).containsExactly(QuestionnaireCurationError(SUBJECT, "WHO status not between 0 and 5: '7'"))
    }

    @Test
    fun `Should not curate WHO status and return error when not a number`() {
        val curated = toWHO(SUBJECT, "string")
        assertThat(curated.curated).isEqualTo(null)
        assertThat(curated.errors).containsExactly(QuestionnaireCurationError(SUBJECT, "WHO status not an integer: 'string'"))
    }

    @Test
    fun `Should extract secondary primary and last treatment date when available`() {
        assertThat(toSecondaryPrimaries("sarcoma", "Feb 2020")).isEqualTo(listOf("sarcoma | last treatment date: Feb 2020"))
    }

    @Test
    fun `Should extract secondary primary when last treatment date not available`() {
        assertThat(toSecondaryPrimaries("sarcoma", "")).isEqualTo(listOf("sarcoma"))
    }

    @Test
    fun `Should return null for empty infection status`() {
        val infectionStatus = toInfectionStatus("")
        assertThat(infectionStatus.curated).isNull()
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun `Should return null for unknown infection status`() {
        val infectionStatus = toInfectionStatus("unknown")
        assertThat(infectionStatus.curated).isNull()
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun `Should return description verbatim for when curation does not exist in infection status`() {
        val infectionStatus = toInfectionStatus("new infection status")
        val curated = infectionStatus.curated!!
        assertThat(curated.description).isEqualTo("new infection status")
        assertThat(curated.hasActiveInfection).isTrue
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun `Should extract positive infection status`() {
        val infectionDescription = "yes"
        val infectionStatus = toInfectionStatus(infectionDescription)
        assertThat(infectionStatus).isNotNull()

        val curated = infectionStatus.curated!!
        assertThat(curated.hasActiveInfection).isTrue()
        assertThat(curated.description).isEqualTo(infectionDescription)
    }

    @Test
    fun `Should extract negative infection status`() {
        val infectionDescription = "no"
        val infectionStatus = toInfectionStatus(infectionDescription)
        assertThat(infectionStatus).isNotNull()

        val curated = infectionStatus.curated!!
        assertThat(curated.hasActiveInfection).isFalse()
        assertThat(curated.description).isEqualTo(infectionDescription)
    }

    @Test
    fun `Should extract infection description and set active`() {
        val infectionDescription = "infection"
        val infectionStatus = toInfectionStatus(infectionDescription)
        assertThat(infectionStatus).isNotNull()

        val curated = infectionStatus.curated!!
        assertThat(curated.hasActiveInfection).isTrue()
        assertThat(curated.description).isEqualTo(infectionDescription)
    }

    @Test
    fun `Should return null for empty ECG`() {
        val ecg = toEcg("")
        assertThat(ecg.curated).isNull()
        assertThat(ecg.errors).isEmpty()
    }

    @Test
    fun `Should return null for unknown ECG`() {
        val ecg = toEcg("unknown")
        assertThat(ecg.curated).isNull()
        assertThat(ecg.errors).isEmpty()
    }

    @Test
    fun `Should return description verbatim for when curation does not exist ECG`() {
        val infectionStatus = toEcg("new ECG value")
        val curated = infectionStatus.curated!!
        assertThat(curated.name).isEqualTo("new ECG value")
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun `Should extract positive ECG`() {
        val description = "yes"
        val status = toEcg(description)
        assertThat(status).isNotNull()

        val curated = status.curated!!
        assertThat(curated.name).isEqualTo(description)
    }

    @Test
    fun `Should extract negative ECG to null`() {
        val (curated, errors) = toEcg("no")
        assertThat(curated).isNull()
        assertThat(errors).isEmpty()
    }

    @Test
    fun `Should extract ECG description and indicate presence`() {
        val description = "ECG"
        val status = toEcg(description)
        assertThat(status).isNotNull()

        val curated = status.curated!!
        assertThat(curated.name).isEqualTo(description)
    }
}