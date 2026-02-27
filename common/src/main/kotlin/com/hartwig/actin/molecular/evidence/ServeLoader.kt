package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.molecular.evidence.ServeCleaner.cleanServeDatabase
import com.hartwig.serve.datamodel.RefGenome
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.serialization.ServeJson

object ServeLoader {

    fun loadServeDatabase(
        jsonFilePath: String,
        removeCombinedProfilesEvidence: Boolean,
        filterServeTrialsByCountry: String? = null
    ): ServeDatabase {
        val serveDatabase = cleanServeDatabase(ServeJson.read(jsonFilePath), removeCombinedProfilesEvidence)

        ServeVerifier.verifyServeDatabase(serveDatabase, removeCombinedProfilesEvidence)

        if (filterServeTrialsByCountry != null) {
            ServeFilter.filterCountriesInServeDatabase(serveDatabase, filterServeTrialsByCountry)
        }

        return serveDatabase
    }

    fun toServeRefGenomeVersion(refGenomeVersion: RefGenomeVersion): RefGenome {
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
