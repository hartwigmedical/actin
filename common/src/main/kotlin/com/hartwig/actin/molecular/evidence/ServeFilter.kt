package com.hartwig.actin.molecular.evidence

import com.hartwig.serve.datamodel.ImmutableServeDatabase
import com.hartwig.serve.datamodel.ImmutableServeRecord
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord

object ServeFilter {

    fun filterCountriesInServeDatabase(database: ServeDatabase, countries: Set<String>): ServeDatabase {

        val filteredRecords = database.records().mapValues { (_, record) ->
            filterCountriesInRecord(record, countries)
        }

        return ImmutableServeDatabase.builder()
            .from(database)
            .records(filteredRecords)
            .build()
    }

    private fun filterCountriesInRecord(record: ServeRecord, countries: Set<String>): ServeRecord {
        val filteredTrials = record.trials().filter { trial ->
            trial.countries().map { country -> country.name() }.intersect(countries).isNotEmpty()
        }

        return ImmutableServeRecord.builder()
            .from(record)
            .trials(filteredTrials)
            .build()
    }

}