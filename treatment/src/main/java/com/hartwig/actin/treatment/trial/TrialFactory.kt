package com.hartwig.actin.treatment.trial

import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.treatment.datamodel.Cohort
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.CriterionReference
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.ImmutableCohort
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference
import com.hartwig.actin.treatment.datamodel.ImmutableEligibility
import com.hartwig.actin.treatment.datamodel.ImmutableTrial
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification
import com.hartwig.actin.treatment.datamodel.Trial
import com.hartwig.actin.treatment.datamodel.TrialIdentification
import com.hartwig.actin.treatment.input.FunctionInputResolver
import com.hartwig.actin.treatment.sort.CohortComparator
import com.hartwig.actin.treatment.sort.CriterionReferenceComparator
import com.hartwig.actin.treatment.sort.EligibilityComparator
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig
import java.io.IOException

class TrialFactory(private val trialModel: TrialConfigModel, private val eligibilityFactory: EligibilityFactory) {
    fun create(): List<Trial> {
        return trialModel.trials().map { trialConfig ->
            val trialId = trialConfig.trialId
            val referencesById = trialModel.referencesForTrial(trialId)
            ImmutableTrial.builder()
                .identification(toIdentification(trialConfig))
                .generalEligibility(toEligibility(trialModel.generalInclusionCriteriaForTrial(trialId), referencesById))
                .cohorts(cohortsForTrial(trialId, referencesById))
                .build()
        }
    }

    private fun cohortsForTrial(trialId: String, referencesById: Map<String, InclusionCriteriaReferenceConfig>): List<Cohort> {
        return trialModel.cohortsForTrial(trialId).map { cohortConfig ->
            val cohortId = cohortConfig.cohortId
            ImmutableCohort.builder()
                .metadata(toMetadata(cohortConfig))
                .eligibility(toEligibility(trialModel.specificInclusionCriteriaForCohort(trialId, cohortId), referencesById))
                .build()
        }
            .sortedWith(CohortComparator())
    }

    private fun toEligibility(
        criteria: List<InclusionCriteriaConfig>,
        referencesById: Map<String, InclusionCriteriaReferenceConfig>
    ): List<Eligibility> {
        return criteria.map { criterion ->
            ImmutableEligibility.builder()
                .references(resolveReferences(referencesById, criterion.referenceIds))
                .function(eligibilityFactory.generateEligibilityFunction(criterion.inclusionRule))
                .build()
        }
            .sortedWith(EligibilityComparator())
    }

    companion object {
        @Throws(IOException::class)
        fun create(trialConfigDirectory: String, doidModel: DoidModel, geneFilter: GeneFilter): TrialFactory {
            val molecularInputChecker = MolecularInputChecker(geneFilter)
            val functionInputResolver = FunctionInputResolver(doidModel, molecularInputChecker)
            val eligibilityFactory = EligibilityFactory(functionInputResolver)
            val trialModel: TrialConfigModel = TrialConfigModel.create(trialConfigDirectory, eligibilityFactory)
            return TrialFactory(trialModel, eligibilityFactory)
        }

        private fun toIdentification(trialConfig: TrialDefinitionConfig): TrialIdentification {
            return ImmutableTrialIdentification.builder()
                .trialId(trialConfig.trialId)
                .open(trialConfig.open)
                .acronym(trialConfig.acronym)
                .title(trialConfig.title)
                .build()
        }

        private fun toMetadata(cohortConfig: CohortDefinitionConfig): CohortMetadata {
            return ImmutableCohortMetadata.builder()
                .cohortId(cohortConfig.cohortId)
                .evaluable(cohortConfig.evaluable)
                .open(cohortConfig.open)
                .slotsAvailable(cohortConfig.slotsAvailable)
                .blacklist(cohortConfig.blacklist)
                .description(cohortConfig.description)
                .build()
        }

        private fun resolveReferences(
            referencesById: Map<String, InclusionCriteriaReferenceConfig>,
            referenceIds: Set<String>
        ): List<CriterionReference> {
            return referenceIds.map { referenceId ->
                if (!referencesById.contains(referenceId)) {
                    throw IllegalStateException("No config found for reference with ID: $referenceId")
                }
                ImmutableCriterionReference.builder().id(referenceId).text(referencesById[referenceId]!!.referenceText).build()
            }
                .sortedWith(CriterionReferenceComparator())
        }
    }
}