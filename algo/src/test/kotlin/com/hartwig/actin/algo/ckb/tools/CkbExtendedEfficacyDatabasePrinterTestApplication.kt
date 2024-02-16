package com.hartwig.actin.algo.ckb.tools

import com.hartwig.actin.algo.ckb.ExtendedEvidenceEntryFactory
import com.hartwig.actin.algo.ckb.datamodel.ExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.json.CkbExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.serialization.CkbExtendedEvidenceJson
import java.io.File

class CkbExtendedEfficacyDatabasePrinterTestApplication {

    fun run() {
        val jsonEntries: List<CkbExtendedEvidenceEntry> = CkbExtendedEvidenceJson.read(EXTENDED_EFFICACY_JSON_PATH)
        jsonEntries.forEach(::println)
        val entries: List<ExtendedEvidenceEntry> = ExtendedEvidenceEntryFactory.extractCkbExtendedEvidence(jsonEntries)
        entries.forEach(::println)
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
    CkbExtendedEfficacyDatabasePrinterTestApplication().run()
}