package com.hartwig.actin.datamodel.molecular.evidence

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

    fun create(
        nctId: String = "",
        title: String = "",
        countries: Set<CountryDetails> = emptySet(),
        molecularMatches: Set<MolecularMatchDetails> = emptySet(),
        applicableCancerTypes: Set<CancerType> = emptySet(),
        url: String = ""
    ): ExternalTrial {
        return ExternalTrial(
            nctId = nctId,
            title = title,
            countries = countries,
            molecularMatches = molecularMatches,
            applicableCancerTypes = applicableCancerTypes,
            url = url
        )
    }
}