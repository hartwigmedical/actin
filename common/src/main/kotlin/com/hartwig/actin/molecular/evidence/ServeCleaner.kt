package com.hartwig.actin.molecular.evidence

import com.hartwig.serve.datamodel.ImmutableServeDatabase
import com.hartwig.serve.datamodel.ImmutableServeRecord
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord

object ServeCleaner {

    fun cleanServeDatabase(database: ServeDatabase): ServeDatabase {
        val cleanedRecords = database.records().mapValues { (_, record) ->
            cleanCombinedTrials(cleanCombinedEvidences(record))
        }

        return ImmutableServeDatabase.builder()
            .from(database)
            .records(cleanedRecords)
            .build()
    }

    private fun cleanCombinedEvidences(record: ServeRecord): ServeRecord {
        val cleanedEvidences = record.evidences().filterNot { evidence ->
            ServeVerifier.isCombinedProfile(evidence.molecularCriterium())
        }

        return ImmutableServeRecord.builder()
            .from(record)
            .evidences(cleanedEvidences)
            .build()
    }

    private fun cleanCombinedTrials(record: ServeRecord): ServeRecord {
        val cleanedTrials = record.trials().filterNot { trial ->
            trial.anyMolecularCriteria().any {
                ServeVerifier.isCombinedProfile(it)
            }
        }
        return ImmutableServeRecord.builder()
            .from(record)
            .trials(cleanedTrials)
            .build()
    }
}