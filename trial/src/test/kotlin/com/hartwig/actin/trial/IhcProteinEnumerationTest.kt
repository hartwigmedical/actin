package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.molecular.UnparameterisedIhcRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

@UnparameterisedIhcRule("PROT1")
class TargetOne
class TargetTwo

@UnparameterisedIhcRule("1TORP")
class TargetThree

class IhcProteinEnumerationTest {
    @Test
    fun `Should construct list of proteins from unparameterised rules`() {
        val reflected = IhcProteinEnumeration().enumerate(emptyList<Trial>())
        assertThat(reflected).contains("PROT1")
        assertThat(reflected).contains("1TORP")
    }
}