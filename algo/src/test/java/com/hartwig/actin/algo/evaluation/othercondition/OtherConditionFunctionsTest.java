package com.hartwig.actin.algo.evaluation.othercondition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.junit.Test;

public class OtherConditionFunctionsTest {

    @Test
    public void canSelectClinicallyRelevant() {
        PriorOtherCondition relevant = OtherConditionTestFactory.builder().isContraindicationForTherapy(true).build();
        PriorOtherCondition irrelevant = OtherConditionTestFactory.builder().isContraindicationForTherapy(false).build();

        List<PriorOtherCondition> filtered = OtherConditionFunctions.selectClinicallyRelevant(Lists.newArrayList(relevant, irrelevant));

        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(relevant));
    }
}