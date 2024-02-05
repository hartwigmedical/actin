package com.hartwig.actin.algo.ckb.serialization

import com.google.common.io.Resources
import com.hartwig.actin.algo.ckb.datamodel.CkbExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.datamodel.CkbExtendedEvidenceTestFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CkbExtendedEvidenceJsonTest {

    private val exampleJson = Resources.getResource("ckb/example_extended_evidence.json").path

    @Test
    fun `Can read example extended efficacy database json file`() {
        val entries: List<CkbExtendedEvidenceEntry> = CkbExtendedEvidenceJson.read(exampleJson)
        assertThat(entries).isNotNull
    }

    @Test
    fun `Can convert proper database back and forth JSON`() {
        val proper = CkbExtendedEvidenceTestFactory.createProperTestExtendedEvidenceDatabase()
        assertThat(CkbExtendedEvidenceJson.fromJson(toJson(proper))).isEqualTo(proper)
    }

    @Test
    fun `Can convert minimal database back and forth JSON`() {
        val minimal = CkbExtendedEvidenceTestFactory.createMinimalTestExtendedEvidenceDatabase()
        assertThat(CkbExtendedEvidenceJson.fromJson(toJson(minimal))).isEqualTo(minimal)
    }

    companion object {
        private fun toJson(entries: List<CkbExtendedEvidenceEntry>): String {
            return CkbExtendedEvidenceJson.createGson().toJson(entries)
        }
    }
}