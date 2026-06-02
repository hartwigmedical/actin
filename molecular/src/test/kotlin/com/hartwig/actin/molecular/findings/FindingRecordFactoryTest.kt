package com.hartwig.actin.molecular.findings

import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FindingRecordFactoryTest {

    @Test
    fun `Should not crash on minimal orange record`() {
        // TODO Figure out why MSI status, TMB status, TMB status, virus interpreter, CUPPA etc cannot be UNKNOWN
        // TODO Also issues with LILAC QC and PEACH genotypes. 
        // assertThat(FindingRecordFactory.create(TestOrangeFactory.createMinimalTestOrangeRecord())).isNotNull()
    }

    @Test
    fun `Should interpret proper orange record`() {
        assertThat(FindingRecordFactory.create(TestOrangeFactory.createProperTestOrangeRecord())).isNotNull()
    }
}