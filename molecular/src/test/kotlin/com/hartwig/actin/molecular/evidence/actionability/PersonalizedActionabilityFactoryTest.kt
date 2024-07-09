package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.serve.datamodel.ActionableEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalizedActionabilityFactoryTest {

    @Test
    fun canDistinguishOnLabelOffLabel() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        val tumorDoids = setOf("child", "blacklist")
        val factory: PersonalizedActionabilityFactory = PersonalizedActionabilityFactory.create(doidModel, tumorDoids)

        val event1 = create("parent", "not blacklisted")
        val event2 = create("other doid")
        val event3 = create("parent", "blacklist")
        val events = listOf(event1, event2, event3)
        val match = factory.create(events)
        assertEquals(1, match.onLabelEvents.size.toLong())
        assertTrue(match.onLabelEvents.contains(event1))
        assertEquals(2, match.offLabelEvents.size.toLong())
        assertTrue(match.offLabelEvents.contains(event2))
        assertTrue(match.offLabelEvents.contains(event3))
    }

    @Test
    fun everythingIsOffLabelWhenTumorDoidsAreUnknown() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val factory: PersonalizedActionabilityFactory = PersonalizedActionabilityFactory.create(doidModel, mutableSetOf())

        val event1 = create("doid 1")
        val event2 = create("doid 2")
        val event3 = create("doid 1", "blacklist")
        val events = listOf(event1, event2, event3)
        val match = factory.create(events)
        assertEquals(0, match.onLabelEvents.size.toLong())
        assertEquals(3, match.offLabelEvents.size.toLong())
    }

    companion object {
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
}