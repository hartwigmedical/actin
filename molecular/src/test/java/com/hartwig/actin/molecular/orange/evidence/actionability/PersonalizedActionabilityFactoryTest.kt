package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.serve.datamodel.ActionableEvent
import org.junit.Assert
import org.junit.Test

class PersonalizedActionabilityFactoryTest {
    @Test
    fun canDistinguishOnLabelOffLabel() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        val tumorDoids: MutableSet<String> = Sets.newHashSet("child", "blacklist")
        val factory: PersonalizedActionabilityFactory = PersonalizedActionabilityFactory.Companion.create(doidModel, tumorDoids)
        val event1 = create("parent", "not blacklisted")
        val event2 = create("other doid")
        val event3 = create("parent", "blacklist")
        val events: MutableList<ActionableEvent> = Lists.newArrayList(event1, event2, event3)
        val match = factory.create(events)
        Assert.assertEquals(1, match.onLabelEvents.size.toLong())
        Assert.assertTrue(match.onLabelEvents.contains(event1))
        Assert.assertEquals(2, match.offLabelEvents.size.toLong())
        Assert.assertTrue(match.offLabelEvents.contains(event2))
        Assert.assertTrue(match.offLabelEvents.contains(event3))
    }

    @Test
    fun everythingIsOffLabelWhenTumorDoidsAreUnknown() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val factory: PersonalizedActionabilityFactory = PersonalizedActionabilityFactory.Companion.create(doidModel, mutableSetOf())
        val event1 = create("doid 1")
        val event2 = create("doid 2")
        val event3 = create("doid 1", "blacklist")
        val events: MutableList<ActionableEvent> = Lists.newArrayList(event1, event2, event3)
        val match = factory.create(events)
        Assert.assertEquals(0, match.onLabelEvents.size.toLong())
        Assert.assertEquals(3, match.offLabelEvents.size.toLong())
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