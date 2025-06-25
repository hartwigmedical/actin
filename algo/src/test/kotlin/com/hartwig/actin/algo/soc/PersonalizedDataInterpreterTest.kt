package com.hartwig.actin.algo.soc

import com.hartwig.actin.datamodel.personalization.TreatmentGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup as PersonalizationTreatmentGroup

class PersonalizedDataInterpreterTest {

    @Test
    fun `Can make all personalization treatment groups to ACTIN treatment groups`() {
        val allEntries = PersonalizationTreatmentGroup.entries
        
        assertThat(allEntries.map { TreatmentGroup.valueOf(it.name) }).hasSize(allEntries.size)
    }
}