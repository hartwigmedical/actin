package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toECG
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toInfectionStatus
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toOption
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toSecondaryPrimaries
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toStage
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toWHO
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val SUBJECT = "subject"

class QuestionnaireCurationTest {

    @Test
    fun `Should curate options when curation exists`() {
        val curated = toOption(SUBJECT, "YES")
        assertThat(curated.curated).isTrue
        assertThat(curated.errors).isEmpty()
    }

    @Test
    fun `Should not curate options and return error when curation does not exist`() {
        val curated = toOption(SUBJECT, "Not an option")
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
        var curated = toOption(SUBJECT, "SusPectEd")
        assertThat(curated.curated).isNull()
        assertThat(curated.errors).isEmpty()

        curated = toOption(SUBJECT, "yEs")
        assertThat(curated.curated).isTrue()
        assertThat(curated.errors).isEmpty()

        curated = toOption(SUBJECT, "botaantasting BIJ weke DELEN massa")
        assertThat(curated.curated).isFalse()
        assertThat(curated.errors).isEmpty()
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
        val infectionStatus = toInfectionStatus(SUBJECT, "")
        assertThat(infectionStatus.curated).isNull()
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun `Should return null for unknown infection status`() {
        val infectionStatus = toInfectionStatus(SUBJECT, "unknown")
        assertThat(infectionStatus.curated).isNull()
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun `Should return description verbatim for when curation does not exist in infection status`() {
        val infectionStatus = toInfectionStatus(SUBJECT, "new infection status")
        val curated = infectionStatus.curated!!
        assertThat(curated.description).isEqualTo("new infection status")
        assertThat(curated.hasActiveInfection).isTrue
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun `Should extract positive infection status`() {
        val infectionDescription = "yes"
        val infectionStatus = toInfectionStatus(SUBJECT, infectionDescription)
        assertThat(infectionStatus).isNotNull()

        val curated = infectionStatus.curated!!
        assertThat(curated.hasActiveInfection).isTrue()
        assertThat(curated.description).isEqualTo(infectionDescription)
    }

    @Test
    fun `Should extract negative infection status`() {
        val infectionDescription = "no"
        val infectionStatus = toInfectionStatus(SUBJECT, infectionDescription)
        assertThat(infectionStatus).isNotNull()

        val curated = infectionStatus.curated!!
        assertThat(curated.hasActiveInfection).isFalse()
        assertThat(curated.description).isEqualTo(infectionDescription)
    }

    @Test
    fun `Should extract infection description and set active`() {
        val infectionDescription = "infection"
        val infectionStatus = toInfectionStatus(SUBJECT, infectionDescription)
        assertThat(infectionStatus).isNotNull()

        val curated = infectionStatus.curated!!
        assertThat(curated.hasActiveInfection).isTrue()
        assertThat(curated.description).isEqualTo(infectionDescription)
    }

    @Test
    fun `Should return null for empty ECG`() {
        val ecg = toECG(SUBJECT, "")
        assertThat(ecg.curated).isNull()
        assertThat(ecg.errors).isEmpty()
    }

    @Test
    fun `Should return null for unknown ECG`() {
        val ecg = toECG(SUBJECT, "unknown")
        assertThat(ecg.curated).isNull()
        assertThat(ecg.errors).isEmpty()
    }

    @Test
    fun `Should return descrioption verbatim for when curation does not exist ECG`() {
        val infectionStatus = toECG(SUBJECT, "new ECG value")
        val curated = infectionStatus.curated!!
        assertThat(curated.aberrationDescription).isEqualTo("new ECG value")
        assertThat(curated.hasSigAberrationLatestECG).isTrue
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun `Should extract positive ECG`() {
        val description = "yes"
        val status = toECG(SUBJECT, description)
        assertThat(status).isNotNull()

        val curated = status.curated!!
        assertThat(curated.hasSigAberrationLatestECG).isTrue()
        assertThat(curated.aberrationDescription).isEqualTo(description)
    }

    @Test
    fun `Should extract negative ECG`() {
        val description = "no"
        val status = toECG(SUBJECT, description)
        assertThat(status).isNotNull()

        val curated = status.curated!!
        assertThat(curated.hasSigAberrationLatestECG).isFalse()
        assertThat(curated.aberrationDescription).isEqualTo(description)
    }

    @Test
    fun `Should extract ECG description and indicate presence`() {
        val description = "ECG"
        val status = toECG(SUBJECT, description)
        assertThat(status).isNotNull()

        val curated = status.curated!!
        assertThat(curated.hasSigAberrationLatestECG).isTrue()
        assertThat(curated.aberrationDescription).isEqualTo(description)
    }
}