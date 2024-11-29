package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.datamodel.clinical.Cyp
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MedicationUtilTest {

    @Test
    fun `Should convert Cyp to string in format where 'CYP' is lost and '_' is replaced`() {
        assertThat(MedicationUtil.extractCypString(Cyp.CYP2C19)).isEqualTo("2C19")
        assertThat(MedicationUtil.extractCypString(Cyp.CYP3A4_5)).isEqualTo("3A4/5")
    }
}