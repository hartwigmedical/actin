package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PersonalizedActionabilityFactoryTest {

    @Test
    fun `Should be able to distinguish on-label and off-label evidence`() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        val tumorDoids = setOf("child", "exclude")
        val factory = PersonalizedActionabilityFactory.create(doidModel, tumorDoids)

        val evidence1 = create("parent", "not excluded")
        val evidence2 = create("other doid")
        val evidence3 = create("parent", "exclude")

        val match = factory.create(ActionabilityMatch(listOf(evidence1, evidence2, evidence3), emptyMap()))

        assertThat(match.treatmentEvidence.filter { it.isOnLabel }.size).isEqualTo(1)
        assertThat(match.treatmentEvidence.filter { !it.isOnLabel }.size).isEqualTo(2)
    }

    @Test
    fun `Should qualify all evidence as off-label if tumor doids are unknown`() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val factory = PersonalizedActionabilityFactory.create(doidModel, emptySet())

        val evidence1 = create("doid 1")
        val evidence2 = create("doid 2")
        val evidence3 = create("doid 1", "exclude")

        val match = factory.create(
            ActionabilityMatch(
                evidenceMatches = listOf(evidence1, evidence2, evidence3),
                matchingCriteriaPerTrialMatch = emptyMap()
            )
        )

        assertThat(match.treatmentEvidence.filter { it.isOnLabel }.size).isEqualTo(0)
        assertThat(match.treatmentEvidence.filter { !it.isOnLabel }.size).isEqualTo(3)
    }

    @Test
    fun `Should determine matching indications for trials`() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        val tumorDoids = setOf("child", "exclude")
        val factory = PersonalizedActionabilityFactory.create(doidModel, tumorDoids)

        val onLabelTrial = TestServeTrialFactory.create(
            nctId = "NCT00000001",
            indications = setOf(
                TestServeFactory.createIndicationWithDoidAndExcludedDoid("parent", "exclude"),
                TestServeFactory.createIndicationWithDoidAndExcludedDoid("child", "not-exclude")
            )
        )

        val offLabelTrial = TestServeTrialFactory.create(
            nctId = "NCT00000002",
            indications = setOf(
                TestServeFactory.createIndicationWithDoidAndExcludedDoid("child", "exclude"),
                TestServeFactory.createIndicationWithDoid("other")
            )
        )

        val result = factory.create(
            ActionabilityMatch(
                evidenceMatches = emptyList(),
                matchingCriteriaPerTrialMatch = mapOf(onLabelTrial to emptySet(), offLabelTrial to emptySet())
            )
        )

        assertThat(result.eligibleTrials.size).isEqualTo(1)
        val eligibleTrial = result.eligibleTrials.first()

        assertThat(eligibleTrial.nctId).isEqualTo(onLabelTrial.nctId())
        assertThat(eligibleTrial.applicableCancerTypes).containsExactly(CancerType("child", setOf("not-exclude")))
    }

    private fun create(doid: String): EfficacyEvidence {
        return TestServeEvidenceFactory.create(indication = TestServeFactory.createIndicationWithDoid(doid))
    }

    private fun create(doid: String, excludedDoid: String): EfficacyEvidence {
        return TestServeEvidenceFactory.create(indication = TestServeFactory.createIndicationWithDoidAndExcludedDoid(doid, excludedDoid))
    }
}