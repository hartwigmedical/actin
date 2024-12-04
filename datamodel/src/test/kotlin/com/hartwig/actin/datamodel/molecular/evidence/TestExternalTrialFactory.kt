package com.hartwig.actin.datamodel.molecular.evidence

import java.time.LocalDate

object TestExternalTrialFactory {

    fun createTestTrial(): ExternalTrial {
        return create(
            nctId = "NCT00000001",
            title = "test trial",
            countries = setOf(
                CountryDetails(Country.NETHERLANDS, mapOf("Leiden" to setOf(Hospital("LUMC", false)))),
                CountryDetails(Country.BELGIUM, mapOf("Brussels" to emptySet()))
            ),
            url = "https://clinicaltrials.gov/study/NCT00000001"
        )
    }

    fun create(nctId: String, title: String, countries: Set<CountryDetails>, url: String): ExternalTrial {
        return ExternalTrial(
            nctId = nctId,
            title = title,
            molecularMatches = setOf(
                MolecularMatchDetails(
                    sourceDate = LocalDate.of(2021, 2, 3),
                    sourceEvent = "",
                    isCategoryEvent = false
                )
            ),
            applicableCancerTypes = setOf(CancerType(matchedCancerType = "", excludedCancerSubTypes = emptySet())),
            countries = countries,
            url = url
        )
    }
}