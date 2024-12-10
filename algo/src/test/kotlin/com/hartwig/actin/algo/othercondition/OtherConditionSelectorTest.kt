package com.hartwig.actin.algo.othercondition

import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OtherConditionSelectorTest {
   
    @Test
    fun canSelectClinicallyRelevant() {
        val relevant = create(true)
        val irrelevant = create(false)
        val filtered = OtherConditionSelector.selectClinicallyRelevant(listOf(relevant, irrelevant))
        assertThat(filtered).containsExactly(relevant)
    }

    private fun create(isContraindicationForTherapy: Boolean): PriorOtherCondition {
        return PriorOtherCondition(
            name = "",
            doids = emptySet(),
            category = "",
            icdMainCode = "",
            icdExtensionCode = null,
            isContraindicationForTherapy = isContraindicationForTherapy,
        )
    }
}