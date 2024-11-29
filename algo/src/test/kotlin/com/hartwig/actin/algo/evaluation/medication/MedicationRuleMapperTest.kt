package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.datamodel.clinical.Cyp
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MedicationRuleMapperTest {

    @Test
    fun `Should accurately convert Cyp to usable cyp-string`() {
        assertThat(MedicationRuleMapper.toCypString(Cyp.CYP2C19)).isEqualTo("2C19")
        assertThat(MedicationRuleMapper.toCypString(Cyp.CYP3A4_5)).isEqualTo("3A4/5")
    }
}