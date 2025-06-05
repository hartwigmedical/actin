package com.hartwig.actin.molecular.evidence

import com.hartwig.serve.datamodel.ImmutableServeDatabase
import com.hartwig.serve.datamodel.ImmutableServeRecord
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.trial.ImmutableActionableTrial

object ServeCleaner {

    fun cleanServeDatabase(database: ServeDatabase): ServeDatabase {
        val cleanedRecords = database.records().mapValues { (_, record) ->
            cleanCombinedTrials(record)
        }

        return ImmutableServeDatabase.builder()
            .from(database)
            .records(cleanedRecords)
            .build()
    }

    private fun cleanCombinedTrials(record: ServeRecord): ServeRecord {
        val cleanedTrials = record.trials().mapNotNull { trial ->
            val filteredCriteria = trial.anyMolecularCriteria().filterNot { ServeVerifier.isCombinedProfile(it) }
            if (filteredCriteria.isNotEmpty()) {
                ImmutableActionableTrial.builder().from(trial)
                    .anyMolecularCriteria(filteredCriteria.toSet())
                    .build()
            } else {
                null
            }
        }
        return ImmutableServeRecord.builder()
            .from(record)
            .trials(cleanedTrials)
            .build()
    }
}