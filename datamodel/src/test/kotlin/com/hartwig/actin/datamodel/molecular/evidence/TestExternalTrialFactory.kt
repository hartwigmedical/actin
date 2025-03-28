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
        source: String = "",
        countries: Set<CountryDetails> = emptySet(),
        molecularMatches: Set<MolecularMatchDetails> = emptySet(),
        applicableCancerTypes: Set<CancerType> = emptySet(),
        therapyNames: Set<String> = emptySet()
    ): ExternalTrial {
        return ExternalTrial(
            nctId = nctId,
            title = title,
            source = source,
            countries = countries,
            molecularMatches = molecularMatches,
            applicableCancerTypes = applicableCancerTypes,
            therapyNames = therapyNames,
        )
    }
}