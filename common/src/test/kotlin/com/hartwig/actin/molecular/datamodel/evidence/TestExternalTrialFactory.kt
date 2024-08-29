package com.hartwig.actin.molecular.datamodel.evidence

object TestExternalTrialFactory {

    fun create(title: String = "", countries: Set<Country> = emptySet(), url: String = "", nctId: String = ""): ExternalTrial {
        return ExternalTrial(
            title = title,
            countries = countries,
            url = url,
            nctId = nctId,
            sourceEvent = "",
            applicableCancerType = ApplicableCancerType(cancerType = "", excludedCancerTypes = emptySet()),
            isCategoryVariant = false
        )
    }

    fun createTestTrial(): ExternalTrial {
        return create(
            "treatment", setOf(createCountry(CountryName.NETHERLANDS, mapOf("Leiden" to setOf("LUMC"))), createCountry(CountryName.BELGIUM, mapOf("Brussels" to emptySet()))), url = "https://clinicaltrials.gov/study/NCT00000001", "NCT00000001"
        )
    }

    fun createCountry(countryName: CountryName, hospitalsPerCity: Map<String, Set<String>> = emptyMap()): Country {
        return Country(countryName, hospitalsPerCity)
    }
}
