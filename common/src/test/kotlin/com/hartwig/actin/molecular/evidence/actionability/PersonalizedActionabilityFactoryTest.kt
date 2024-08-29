package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.ActionableEvent
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
        val match = factory.create(events)
        assertThat(match.onLabelEvents.size).isEqualTo(1)
        assertThat(match.onLabelEvents).contains(event1)
        assertThat(match.offLabelEvents.size).isEqualTo(2)
        assertThat(match.offLabelEvents).contains(event2)
        assertThat(match.offLabelEvents).contains(event3)
    }

    @Test
    fun `Should qualify everything as off-label if tumor doids are unknown`() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val factory: PersonalizedActionabilityFactory = PersonalizedActionabilityFactory.create(doidModel, mutableSetOf())

        val event1 = create("doid 1")
        val event2 = create("doid 2")
        val event3 = create("doid 1", "blacklist")
        val events = listOf(event1, event2, event3)
        val match = factory.create(events)
        assertThat(match.onLabelEvents.size).isEqualTo(0)
        assertThat(match.offLabelEvents.size).isEqualTo(3)
    }

    private fun create(doid: String): ActionableEvent {
        return TestServeActionabilityFactory.hotspotBuilder()
            .applicableCancerType(TestServeActionabilityFactory.cancerTypeBuilder().doid(doid).build())
            .build()
    }

    private fun create(doid: String, blacklistDoid: String): ActionableEvent {
        return TestServeActionabilityFactory.hotspotBuilder()
            .from(create(doid))
            .addBlacklistCancerTypes(TestServeActionabilityFactory.cancerTypeBuilder().doid(blacklistDoid).build())
            .build()
    }
}