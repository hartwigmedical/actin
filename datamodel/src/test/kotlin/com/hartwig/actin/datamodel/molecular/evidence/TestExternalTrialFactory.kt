package com.hartwig.actin.datamodel.molecular.evidence

object TestExternalTrialFactory {

    fun create(title: String = "", countries: Set<Country> = emptySet(), url: String = "", nctId: String = ""): ExternalTrial {
        return ExternalTrial(
            title = title,
            countries = countries,
            url = url,
            nctId = nctId,
            sourceEvent = "",
            applicableCancerType = ApplicableCancerType(cancerType = "", excludedCancerTypes = emptySet()),
            isCategoryEvent = false
        )
    }

    fun createTestTrial(): ExternalTrial {
        return create(
            "treatment", setOf(Country.NETHERLANDS, Country.BELGIUM), url = "https://clinicaltrials.gov/study/NCT00000001", "NCT00000001"
        )
    }
}
