package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toEcg
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCuration.toInfectionStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class QuestionnaireCurationTest {


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