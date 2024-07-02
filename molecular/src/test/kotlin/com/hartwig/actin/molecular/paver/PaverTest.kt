package com.hartwig.actin.molecular.paver

import org.junit.Test

class PaverTest {
    @Test
    fun `Should run paver`() {
        val p = Paver()
        p.pave()
        p.loadPaveVcf("/tmp/kz/actin-transvar.pave.vcf.gz")
    }
}