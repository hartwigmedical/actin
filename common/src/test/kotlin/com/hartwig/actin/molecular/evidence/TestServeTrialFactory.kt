package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.common.ImmutableCancerType
import com.hartwig.serve.datamodel.common.ImmutableIndication
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial
import com.hartwig.serve.datamodel.trial.Country
import com.hartwig.serve.datamodel.trial.GenderCriterium
import com.hartwig.serve.datamodel.trial.ImmutableCountry

object TestServeTrialFactory {

    fun create(
        molecularCriteria: Set<MolecularCriterium>,
        source: Knowledgebase = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
        title: String = "",
        indications: Set<Indication> = setOf(
            ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("").doid("").build())
                .excludedSubTypes(emptySet()).build()
        )
    ): ActionableTrial {
        return object : ActionableTrial() {
            override fun source(): Knowledgebase {
                return source
            }

            override fun nctId(): String {
                return "NCT00000001"
            }

            override fun title(): String {
                return title
            }

            override fun acronym(): String {
                return title
            }

            override fun countries(): Set<Country> {
                return setOf(ImmutableCountry.builder().name("country").build())
            }

            override fun therapyNames(): Set<String> {
                return setOf(title)
            }

            override fun genderCriterium(): GenderCriterium? {
                return null
            }

            override fun indications(): Set<Indication> {
                return indications
            }

            override fun anyMolecularCriteria(): Set<MolecularCriterium> {
                return molecularCriteria
            }

            override fun urls(): Set<String> {
                return setOf("https://clinicaltrials.gov/study/NCT00000001")
            }
        }
    }
}