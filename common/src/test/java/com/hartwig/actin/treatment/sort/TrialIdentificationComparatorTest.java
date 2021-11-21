package com.hartwig.actin.treatment.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;

import org.junit.Test;

public class TrialIdentificationComparatorTest {

    @Test
    public void canSortTrialIdentifications() {
        TrialIdentification identification1 =
                ImmutableTrialIdentification.builder().trialId("1").acronym("First").title("Real First").build();
        TrialIdentification identification2 =
                ImmutableTrialIdentification.builder().trialId("1").acronym("First").title("Wants to be first").build();
        TrialIdentification identification3 = ImmutableTrialIdentification.builder().trialId("1").acronym("Second").title("Second").build();
        TrialIdentification identification4 = ImmutableTrialIdentification.builder().trialId("2").acronym("Third").title("Third").build();

        List<TrialIdentification> identifications = Lists.newArrayList(identification3, identification1, identification4, identification2);
        identifications.sort(new TrialIdentificationComparator());

        assertEquals(identification1, identifications.get(0));
        assertEquals(identification2, identifications.get(1));
        assertEquals(identification3, identifications.get(2));
        assertEquals(identification4, identifications.get(3));
    }
}