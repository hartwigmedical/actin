package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial
import com.hartwig.serve.datamodel.trial.ImmutableActionableTrial
import com.hartwig.serve.datamodel.trial.ImmutableCountry

object TestServeTrialFactory {

    fun create(
        source: Knowledgebase = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
        title: String = "",
        indications: Set<Indication> = setOf(TestServeFactory.createEmptyIndication()),
        molecularCriteria: Set<MolecularCriterium>
    ): ActionableTrial {
        return ImmutableActionableTrial.builder()
            .source(source)
            .nctId("NCT00000001")
            .title(title)
            .acronym(title)
            .countries(setOf(ImmutableCountry.builder().name("country").build()))
            .therapyNames(setOf(title))
            .genderCriterium(null)
            .indications(indications)
            .anyMolecularCriteria(molecularCriteria)
            .urls(setOf("https://clinicaltrials.gov/study/NCT00000001"))
            .build()
    }
}