package com.hartwig.actin.algo.othercondition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OtherConditionSelectorTest {

    @Test
    public void canSelectClinicallyRelevant() {
        PriorOtherCondition relevant = create(true);
        PriorOtherCondition irrelevant = create(false);

        List<PriorOtherCondition> filtered = OtherConditionSelector.selectClinicallyRelevant(Lists.newArrayList(relevant, irrelevant));

        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(relevant));
    }

    @NotNull
    private static PriorOtherCondition create(boolean isContraindicationForTherapy) {
        return ImmutablePriorOtherCondition.builder()
                .name(Strings.EMPTY)
                .category(Strings.EMPTY)
                .isContraindicationForTherapy(isContraindicationForTherapy)
                .build();
    }
}