package com.hartwig.actin.algo.ckb

import com.hartwig.actin.algo.ckb.datamodel.CkbExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.json.JsonCkbExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.serialization.CkbExtendedEvidenceJson
import java.io.File

class CkbExtendedEfficacyDatabaseApplication {

    fun run() {
        val jsonEntries: List<JsonCkbExtendedEvidenceEntry> = CkbExtendedEvidenceJson.read(EXTENDED_EFFICACY_JSON_PATH)
        val entries: List<CkbExtendedEvidenceEntry> = CkbExtendedEvidenceEntryFactory().extractCkbExtendedEvidence(jsonEntries)
    }

    companion object {
        private val EXTENDED_EFFICACY_JSON_PATH = listOf(
            System.getProperty("user.home"),
            "hmf",
            "repos",
            "actin-resources-private",
            "ckb_extended_efficacy",
            "extended_ee_output.json"
        ).joinToString(File.separator)
    }
}

fun main() {
    CkbExtendedEfficacyDatabaseApplication().run()
}