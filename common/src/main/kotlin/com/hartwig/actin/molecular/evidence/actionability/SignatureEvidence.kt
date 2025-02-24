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

    fun findHomologousRecombinationMatches(isHomologousRecombinationDeficient: Boolean): ActionabilityMatch {
        return findMatches(isHomologousRecombinationDeficient, TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
    }

    fun findTumorBurdenMatches(hasHighTumorMutationalBurden: Boolean): ActionabilityMatch {
        return findMatches(hasHighTumorMutationalBurden, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
    }

    fun findTumorLoadMatches(hasHighTumorMutationalLoad: Boolean): ActionabilityMatch {
        return findMatches(hasHighTumorMutationalLoad, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
    }

    private fun findMatches(hasCharacteristic: Boolean, characteristicToFind: TumorCharacteristicType): ActionabilityMatch {
        return if (hasCharacteristic) {
            val matchPredicate: Predicate<MolecularCriterium> =
                Predicate { ActionableEventExtraction.extractCharacteristic(it).type() == characteristicToFind }

            val evidenceMatches = signatureEvidences.filter { matchPredicate.test(it.molecularCriterium()) }
            val matchingCriteriaPerTrialMatch = signatureTrialMatcher.apply(matchPredicate)

            ActionabilityMatch(evidenceMatches = evidenceMatches, matchingCriteriaPerTrialMatch = matchingCriteriaPerTrialMatch)
        } else {
            ActionabilityMatch(evidenceMatches = emptyList(), matchingCriteriaPerTrialMatch = emptyMap())
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
                EfficacyEvidenceExtractor.extractCharacteristicEvidence(evidences, SIGNATURE_CHARACTERISTICS_TYPES)
            val characteristicsTrialMatcher =
                ActionableTrialMatcherFactory.createCharacteristicsTrialMatcher(trials, SIGNATURE_CHARACTERISTICS_TYPES)

            return SignatureEvidence(characteristicsEvidences, characteristicsTrialMatcher)
        }
    }
}
