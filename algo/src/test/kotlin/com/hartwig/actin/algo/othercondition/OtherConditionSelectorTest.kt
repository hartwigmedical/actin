package com.hartwig.actin.algo.othercondition

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import org.apache.logging.log4j.util.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OtherConditionSelectorTest {
    @Test
    fun canSelectClinicallyRelevant() {
        val relevant = create(true)
        val irrelevant = create(false)
        val filtered = OtherConditionSelector.selectClinicallyRelevant(Lists.newArrayList(relevant, irrelevant))
        assertEquals(1, filtered.size.toLong())
        assertTrue(filtered.contains(relevant))
    }

    companion object {
        private fun create(isContraindicationForTherapy: Boolean): PriorOtherCondition {
            return ImmutablePriorOtherCondition.builder()
                .name(Strings.EMPTY)
                .category(Strings.EMPTY)
                .isContraindicationForTherapy(isContraindicationForTherapy)
                .build()
        }
    }
}