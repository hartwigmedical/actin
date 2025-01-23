package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

private val ALWAYS_VALID_PREDICATE: Predicate<MolecularCriterium> = Predicate { true }

class VirusEvidence(
    private val hpvEvidences: List<EfficacyEvidence>,
    private val hpvTrialMatcher: ActionableTrialMatcher,
    private val ebvEvidences: List<EfficacyEvidence>,
    private val ebvTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<Virus> {

    override fun findMatches(event: Virus): ActionabilityMatch {
        return if (!event.isReportable) {
            ActionabilityMatch(evidenceMatches = emptyList(), matchingCriteriaPerTrialMatch = emptyMap())
        } else when (event.type) {
            VirusType.HUMAN_PAPILLOMA_VIRUS -> {
                ActionabilityMatch(
                    evidenceMatches = hpvEvidences,
                    matchingCriteriaPerTrialMatch = hpvTrialMatcher.apply(ALWAYS_VALID_PREDICATE)
                )
            }

            VirusType.EPSTEIN_BARR_VIRUS -> {
                ActionabilityMatch(
                    evidenceMatches = ebvEvidences,
                    matchingCriteriaPerTrialMatch = ebvTrialMatcher.apply(ALWAYS_VALID_PREDICATE)
                )
            }

            else -> {
                ActionabilityMatch(evidenceMatches = emptyList(), matchingCriteriaPerTrialMatch = emptyMap())
            }
        }
    }

    companion object {
        private val HPV_POSITIVE_TYPES = setOf(TumorCharacteristicType.HPV_POSITIVE)
        private val EBV_POSITIVE_TYPES = setOf(TumorCharacteristicType.EBV_POSITIVE)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): VirusEvidence {
            val hpvEvidences = EfficacyEvidenceExtractor.extractCharacteristicEvidence(evidences, HPV_POSITIVE_TYPES)
            val hpvTrialMatcher = ActionableTrialMatcherFactory.createCharacteristicsTrialMatcher(trials, HPV_POSITIVE_TYPES)

            val ebvEvidences = EfficacyEvidenceExtractor.extractCharacteristicEvidence(evidences, EBV_POSITIVE_TYPES)
            val ebvTrialMatcher = ActionableTrialMatcherFactory.createCharacteristicsTrialMatcher(trials, EBV_POSITIVE_TYPES)

            return VirusEvidence(
                hpvEvidences,
                hpvTrialMatcher,
                ebvEvidences,
                ebvTrialMatcher
            )
        }
    }
}
