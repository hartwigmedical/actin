package com.hartwig.actin.molecular.orange.evidence.actionability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.serve.datamodel.ActionableEvent;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class PersonalizedActionabilityFactoryTest {

    @Test
    public void canDistinguishOnLabelOffLabel() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child");
        Set<String> tumorDoids = Sets.newHashSet("child", "blacklist");

        PersonalizedActionabilityFactory factory = PersonalizedActionabilityFactory.create(doidModel, tumorDoids);

        ActionableEvent event1 = create("parent", "not blacklisted");
        ActionableEvent event2 = create("other doid");
        ActionableEvent event3 = create("parent", "blacklist");

        List<ActionableEvent> events = Lists.newArrayList(event1, event2, event3);

        ActionabilityMatch match = factory.create(events);
        assertEquals(1, match.onLabelEvents().size());
        assertTrue(match.onLabelEvents().contains(event1));

        assertEquals(2, match.offLabelEvents().size());
        assertTrue(match.offLabelEvents().contains(event2));
        assertTrue(match.offLabelEvents().contains(event3));
    }

    @Test
    public void everythingIsOffLabelWhenTumorDoidsAreUnknown() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        PersonalizedActionabilityFactory factory = PersonalizedActionabilityFactory.create(doidModel, null);

        ActionableEvent event1 = create("doid 1");
        ActionableEvent event2 = create("doid 2");
        ActionableEvent event3 = create("doid 1", "blacklist");

        List<ActionableEvent> events = Lists.newArrayList(event1, event2, event3);

        ActionabilityMatch match = factory.create(events);
        assertEquals(0, match.onLabelEvents().size());
        assertEquals(3, match.offLabelEvents().size());
    }

    @NotNull
    private static ActionableEvent create(@NotNull String doid) {
        return TestServeActionabilityFactory.hotspotBuilder()
                .applicableCancerType(TestServeActionabilityFactory.cancerTypeBuilder().doid(doid).build())
                .build();
    }

    @NotNull
    private static ActionableEvent create(@NotNull String doid, @NotNull String blacklistDoid) {
        return TestServeActionabilityFactory.hotspotBuilder()
                .from(create(doid))
                .addBlacklistCancerTypes(TestServeActionabilityFactory.cancerTypeBuilder().doid(blacklistDoid).build())
                .build();
    }
}