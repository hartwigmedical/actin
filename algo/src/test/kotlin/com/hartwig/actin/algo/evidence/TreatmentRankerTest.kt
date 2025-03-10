package com.hartwig.actin.algo.evidence

import com.hartwig.actin.datamodel.TestPatientFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentRankerTest {

    @Test
    fun `Should add scoring to single treatment`() {
        val ranker = TreatmentRanker()
        val rank = ranker.rank(TestPatientFactory.createProperTestPatientRecord())
        assertThat(rank).isNotEmpty
    }

}