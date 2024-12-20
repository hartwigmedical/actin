package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActionabilityMatchFactoryTest {

    @Test
    fun `Should flatten evidence when creating actionability match`() {
        val evidence1 = TestServeEvidenceFactory.createEvidenceForHotspot()
        val evidence2 = TestServeEvidenceFactory.createEvidenceForHotspot()
        val evidenceMatches1 = listOf(evidence1)
        val evidenceMatches2 = listOf(evidence2)

        val result = ActionabilityMatchFactory.create(
            evidenceMatchLists = listOf(evidenceMatches1, evidenceMatches2),
            matchingCriteriaPerTrialMatchLists = emptyList()
        )

        assertThat(result.evidenceMatches).containsExactly(evidence1, evidence2)
        assertThat(result.matchingCriteriaPerTrialMatch).isEmpty()
    }

    @Test
    fun `Should aggregate lists of trial matching by actionable trial`() {
        val trial1 = TestServeTrialFactory.create(nctId = "nct 01")
        val trial2 = TestServeTrialFactory.create(nctId = "nct 02")

        val criterium1 = TestServeMolecularFactory.createHotspotCriterium()
        val criterium2 = TestServeMolecularFactory.createCodonCriterium()
        val criterium3 = TestServeMolecularFactory.createExonCriterium()

        val matchingCriteriaPerTrialMatch1 = mapOf(trial1 to setOf(criterium1, criterium2))
        val matchingCriteriaPerTrialMatch2 = mapOf(trial1 to setOf(criterium3))
        val matchingCriteriaPerTrialMatch3 = mapOf(trial2 to setOf(criterium1))

        val result = ActionabilityMatchFactory.create(
            evidenceMatchLists = emptyList(),
            matchingCriteriaPerTrialMatchLists = listOf(
                matchingCriteriaPerTrialMatch1,
                matchingCriteriaPerTrialMatch2,
                matchingCriteriaPerTrialMatch3
            )
        )

        assertThat(result.evidenceMatches).isEmpty()
        assertThat(result.matchingCriteriaPerTrialMatch).isEqualTo(
            mapOf(
                trial1 to setOf(criterium1, criterium2, criterium3),
                trial2 to setOf(criterium1)
            )
        )
    }
}