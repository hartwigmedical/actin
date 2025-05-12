package com.hartwig.actin.datamodel.molecular.evidence

object TestExternalTrialFactory {

    fun createTestTrial(): ExternalTrial {
        return create(
            nctId = "NCT00000001",
            title = "test trial",
            countries = setOf(
                CountryDetails(Country.NETHERLANDS, mapOf("Leiden" to setOf(Hospital("LUMC", false)))),
                CountryDetails(Country.BELGIUM, mapOf("Brussels" to emptySet()))
            )
        )
    }

    fun create(
        nctId: String = "",
        title: String = "",
        acronym: String? = null,
        source: String = "",
        treatments: Set<String> = emptySet(),
        countries: Set<CountryDetails> = emptySet(),
        molecularMatches: Set<MolecularMatchDetails> = emptySet(),
        applicableCancerTypes: Set<CancerType> = emptySet(),
        url: String = ""
    ): ExternalTrial {
        return ExternalTrial(
            nctId = nctId,
            title = title,
            acronym = acronym,
            source = source,
            treatments = treatments,
            countries = countries,
            molecularMatches = molecularMatches,
            applicableCancerTypes = applicableCancerTypes,
        )
    }
}