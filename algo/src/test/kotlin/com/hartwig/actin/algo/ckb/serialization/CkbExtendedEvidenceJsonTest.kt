package com.hartwig.actin.algo.ckb.serialization

import com.hartwig.actin.algo.ckb.json.CkbExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.json.CkbExtendedEvidenceTestFactory
import com.hartwig.actin.testutil.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CkbExtendedEvidenceJsonTest {

    @Test
    fun `Can read example extended efficacy database json file`() {
        val exampleJson = resourceOnClasspath("ckb/example_extended_evidence.json")
        val entries: List<CkbExtendedEvidenceEntry> = CkbExtendedEvidenceJson.read(exampleJson)
        val proper = CkbExtendedEvidenceTestFactory.createProperTestExtendedEvidenceDatabase()

        assertThat(entries).isEqualTo(proper)
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