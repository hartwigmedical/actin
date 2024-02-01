package com.hartwig.actin.algo.ckb.serialization

import com.google.common.io.Resources
import com.hartwig.actin.algo.ckb.datamodel.CkbExtendedEvidenceEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CkbExtendedEvidenceJsonTest {

    private val exampleJson = Resources.getResource("ckb/example_extended_evidence.json").path

    @Test
    fun `Can correctly read example extended efficacy database`() {
        val entries: List<CkbExtendedEvidenceEntry> = CkbExtendedEvidenceJson.read(exampleJson)
        assertThat(entries).isNotNull
    }
}