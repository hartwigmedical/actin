package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.molecular.evidence.ServeCleaner.cleanServeDatabase
import com.hartwig.serve.datamodel.RefGenome
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.serialization.ServeJson

object ServeLoader {

    fun loadServeDatabase(jsonFilePath: String): ServeDatabase {
        val serveDatabase = cleanServeDatabase(ServeJson.read(jsonFilePath))

        ServeVerifier.verifyServeDatabase(serveDatabase)

        return serveDatabase
    }

    fun loadServeRecord(jsonFilePath: String, refGenomeVersion: RefGenomeVersion): ServeRecord {
        val serveDatabase = loadServeDatabase(jsonFilePath)
        val serveRefGenomeVersion = toServeRefGenomeVersion(refGenomeVersion)

        return serveDatabase.records()[serveRefGenomeVersion]
            ?: throw IllegalStateException("No serve record for ref genome version $serveRefGenomeVersion")
    }

    fun loadServe37Record(jsonFilePath: String): ServeRecord {
        return loadServeRecord(jsonFilePath, RefGenomeVersion.V37)
    }

    private fun toServeRefGenomeVersion(refGenomeVersion: RefGenomeVersion): RefGenome {
        return when (refGenomeVersion) {
            RefGenomeVersion.V37 -> {
                RefGenome.V37
            }

            RefGenomeVersion.V38 -> {
                RefGenome.V38
            }
        }
    }
}
