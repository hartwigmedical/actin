package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial
import com.hartwig.serve.datamodel.trial.Country
import com.hartwig.serve.datamodel.trial.ImmutableActionableTrial

object TestServeTrialFactory {

    fun create(
        source: Knowledgebase = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
        nctId: String = "NCT00000001",
        title: String = "",
        acronym: String = "",
        countries: Set<Country> = emptySet(),
        indications: Set<Indication> = emptySet(),
        anyMolecularCriteria: Set<MolecularCriterium> = emptySet(),
        urls: Set<String> = setOf("https://clinicaltrials.gov/study/$nctId")
    ): ActionableTrial {
        return ImmutableActionableTrial.builder()
            .source(source)
            .nctId(nctId)
            .title(title)
            .acronym(acronym)
            .countries(countries)
            .therapyNames(emptySet())
            .genderCriterium(null)
            .indications(indications)
            .anyMolecularCriteria(anyMolecularCriteria)
            .urls(urls)
            .build()
    }
}