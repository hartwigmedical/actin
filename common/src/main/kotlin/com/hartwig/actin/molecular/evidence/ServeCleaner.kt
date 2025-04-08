package com.hartwig.actin.molecular.evidence

import com.hartwig.serve.datamodel.ImmutableServeDatabase
import com.hartwig.serve.datamodel.ImmutableServeRecord
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord

object ServeCleaner {

    fun cleanServeDatabase(database: ServeDatabase): ServeDatabase {
        val cleanedRecords = database.records().mapValues { (_, record) ->
            cleanCombinedEvidences(record)
        }

        return ImmutableServeDatabase.builder()
            .from(database)
            .records(cleanedRecords)
            .build()
    }

    fun cleanCombinedEvidences(record: ServeRecord): ServeRecord {
        val cleanedEvidences = record.evidences().filterNot { evidence ->
            ServeVerifier.isCombinedProfile(evidence.molecularCriterium())
        }

        return ImmutableServeRecord.builder()
            .from(record)
            .evidences(cleanedEvidences)
            .build()
    }
}