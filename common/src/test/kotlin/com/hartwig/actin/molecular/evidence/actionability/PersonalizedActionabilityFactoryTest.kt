package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.common.ImmutableCancerType
import com.hartwig.serve.datamodel.common.ImmutableIndication
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PersonalizedActionabilityFactoryTest {

    @Test
    fun `Should be able to distinguish on-label and off-label`() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        val tumorDoids = setOf("child", "blacklist")
        val factory: PersonalizedActionabilityFactory = PersonalizedActionabilityFactory.create(doidModel, tumorDoids)

        val event1 = create("parent", "not blacklisted")
        val event2 = create("other doid")
        val event3 = create("parent", "blacklist")
        val events = listOf(event1, event2, event3)
        val match = factory.create(ActionableEvents(events, emptyList()))

        assertThat(match.onLabelEvidences.size).isEqualTo(1)
        assertThat(match.onLabelEvidences).containsExactly(event1)
        assertThat(match.offLabelEvidences.size).isEqualTo(2)
        assertThat(match.offLabelEvidences).containsExactly(event2, event3)
    }

    @Test
    fun `Should qualify everything as off-label if tumor doids are unknown`() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val factory: PersonalizedActionabilityFactory = PersonalizedActionabilityFactory.create(doidModel, mutableSetOf())

        val event1 = create("doid 1")
        val event2 = create("doid 2")
        val event3 = create("doid 1", "blacklist")
        val events = listOf(event1, event2, event3)
        val match = factory.create(ActionableEvents(events, emptyList()))
        assertThat(match.onLabelEvidence.evidences.size).isEqualTo(0)
        assertThat(match.offLabelEvidence.evidences.size).isEqualTo(3)
    }

    private fun create(doid: String): EfficacyEvidence {
        val molecularCriterium = TestServeActionabilityFactory.createHotspot()
        val indication = ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("").doid(doid).build())
            .excludedSubTypes(emptySet()).build()
        return TestServeActionabilityFactory.createEfficacyEvidence(molecularCriterium, indication = indication)
    }

    private fun create(doid: String, blacklistDoid: String): EfficacyEvidence {
        val molecularCriterium = TestServeActionabilityFactory.createHotspot()
        val indication = ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("").doid(doid).build())
            .excludedSubTypes(setOf(ImmutableCancerType.builder().name("").doid(blacklistDoid).build())).build()
        return TestServeActionabilityFactory.createEfficacyEvidence(molecularCriterium, indication = indication)
    }
}