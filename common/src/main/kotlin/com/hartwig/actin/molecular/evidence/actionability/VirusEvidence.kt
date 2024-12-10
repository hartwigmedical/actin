package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.trial.ActionableTrial

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
                ActionabilityMatch(evidenceMatches = hpvEvidences, hpvTrialMatcher.matchTrials({ true }))
            }

            VirusType.EPSTEIN_BARR_VIRUS -> {
                ActionabilityMatch(evidenceMatches = ebvEvidences, ebvTrialMatcher.matchTrials({ true }))
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
            val hpvEvidences = ActionableEventsExtraction.extractCharacteristicEvidence(evidences, HPV_POSITIVE_TYPES)
            val (hpvTrials, hpvPredicate) = ActionableEventsExtraction.extractCharacteristicsTrials(trials, HPV_POSITIVE_TYPES)
            val hpvTrialMatcher = ActionableTrialMatcher(hpvTrials, hpvPredicate)

            val ebvEvidences = ActionableEventsExtraction.extractCharacteristicEvidence(evidences, EBV_POSITIVE_TYPES)
            val (ebvTrials, ebvPredicate) = ActionableEventsExtraction.extractCharacteristicsTrials(trials, EBV_POSITIVE_TYPES)
            val ebvTrialMatcher = ActionableTrialMatcher(ebvTrials, ebvPredicate)

            return VirusEvidence(
                hpvEvidences,
                hpvTrialMatcher,
                ebvEvidences,
                ebvTrialMatcher
            )
        }
    }
}
