package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class SignatureEvidence(
    private val signatureEvidences: List<EfficacyEvidence>,
    private val signatureTrialMatcher: ActionableTrialMatcher
) {

    fun findMicrosatelliteMatches(isMicrosatelliteUnstable: Boolean): ActionabilityMatch {
        return findMatches(isMicrosatelliteUnstable, TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
    }

    fun findHomologousRepairMatches(isHomologousRepairDeficient: Boolean): ActionabilityMatch {
        return findMatches(isHomologousRepairDeficient, TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
    }

    fun findTumorBurdenMatches(hasHighTumorMutationalBurden: Boolean): ActionabilityMatch {
        return findMatches(hasHighTumorMutationalBurden, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
    }

    fun findTumorLoadMatches(hasHighTumorMutationalLoad: Boolean): ActionabilityMatch {
        return findMatches(hasHighTumorMutationalLoad, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
    }

    private fun findMatches(
        hasCharacteristic: Boolean,
        typeToFind: TumorCharacteristicType
    ): ActionabilityMatch {
        return if (!hasCharacteristic) ActionabilityMatch(emptyList(), emptyMap()) else {
            val matchPredicate: Predicate<MolecularCriterium> =
                Predicate { ActionableEventsExtraction.extractCharacteristic(it).type() == typeToFind }

            val evidenceMatches = signatureEvidences.filter { matchPredicate.test(it.molecularCriterium()) }
            val trialMatches = signatureTrialMatcher.matchTrials(matchPredicate)

            ActionabilityMatch(evidenceMatches, trialMatches)
        }
    }

    companion object {
        private val SIGNATURE_CHARACTERISTICS_TYPES = setOf(
            TumorCharacteristicType.MICROSATELLITE_UNSTABLE,
            TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT,
            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN,
            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD
        )

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): SignatureEvidence {
            val characteristicsEvidences =
                ActionableEventsExtraction.extractCharacteristicEvidence(evidences, SIGNATURE_CHARACTERISTICS_TYPES)
            val (characteristicsTrials, characteristicsPredicate) = ActionableEventsExtraction.extractCharacteristicsTrials(
                trials,
                SIGNATURE_CHARACTERISTICS_TYPES
            )
            val characteristicsTrialMatcher = ActionableTrialMatcher(characteristicsTrials, characteristicsPredicate)

            return SignatureEvidence(characteristicsEvidences, characteristicsTrialMatcher)
        }
    }
}
