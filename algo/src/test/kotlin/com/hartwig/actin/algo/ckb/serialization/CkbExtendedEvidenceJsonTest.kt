package com.hartwig.actin.algo.ckb.serialization

import com.google.common.io.Resources
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class CkbExtendedEvidenceJsonTest {

    private val evidenceDirectory = Resources.getResource("ckb_extended_evidence").path
    private val exampleJson = evidenceDirectory + File.separator + "example_extended_evidence.json"

    @Test
    fun `Can correctly read example extended efficacy database`() {
        assertThat(CkbExtendedEvidenceJson.read(exampleJson)).isNotNull
    }
}