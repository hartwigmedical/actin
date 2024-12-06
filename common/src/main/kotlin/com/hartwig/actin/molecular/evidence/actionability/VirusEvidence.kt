package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.trial.ActionableTrial

class VirusEvidence(
    private val hpvEvidences: List<EfficacyEvidence>,
    private val hpvTrials: List<ActionableTrial>,
    private val ebvEvidences: List<EfficacyEvidence>,
    private val ebvTrials: List<ActionableTrial>
) : ActionabilityMatcher<Virus> {

    override fun findMatches(event: Virus): ActionabilityMatch {
        return if (!event.isReportable) {
            ActionabilityMatch(evidenceMatches = emptyList(), matchingCriteriaPerTrialMatch = emptyMap())
        } else when (event.type) {
            VirusType.HUMAN_PAPILLOMA_VIRUS -> {
                ActionabilityMatch(evidenceMatches = hpvEvidences, matchingCriteriaPerTrialMatch = hpvTrials)
            }

            VirusType.EPSTEIN_BARR_VIRUS -> {
                ActionabilityMatch(evidenceMatches = ebvEvidences, matchingCriteriaPerTrialMatch = ebvTrials)
            }

            else -> {
                ActionabilityMatch(evidenceMatches = emptyList(), matchingCriteriaPerTrialMatch = emptyList())
            }
        }
    }

    companion object {
        private val HPV_POSITIVE_TYPES = setOf(TumorCharacteristicType.HPV_POSITIVE)
        private val EBV_POSITIVE_TYPES = setOf(TumorCharacteristicType.EBV_POSITIVE)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): VirusEvidence {
            val hpvCharacteristicsEvidence = ActionableEventsExtraction.extractCharacteristicEvidence(evidences, HPV_POSITIVE_TYPES)
            val hpvCharacteristicsTrials = ActionableEventsExtraction.extractCharacteristicsTrials(trials, HPV_POSITIVE_TYPES)
            val ebvCharacteristicsEvidence = ActionableEventsExtraction.extractCharacteristicEvidence(evidences, EBV_POSITIVE_TYPES)
            val ebvCharacteristicsTrials = ActionableEventsExtraction.extractCharacteristicsTrials(trials, EBV_POSITIVE_TYPES)

            return VirusEvidence(
                hpvCharacteristicsEvidence,
                hpvCharacteristicsTrials,
                ebvCharacteristicsEvidence,
                ebvCharacteristicsTrials
            )
        }
    }
}
