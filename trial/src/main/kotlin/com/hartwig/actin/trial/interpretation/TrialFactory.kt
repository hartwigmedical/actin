package com.hartwig.actin.trial.interpretation

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.treatment.datamodel.Cohort
import com.hartwig.actin.treatment.datamodel.CriterionReference
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.ImmutableCohort
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
import com.hartwig.actin.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.trial.config.TrialConfigModel
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.ctc.CTCModel
import java.io.IOException

class TrialFactory(
    private val trialConfigModel: TrialConfigModel,
    private val ctcModel: CTCModel,
    private val eligibilityFactory: EligibilityFactory
) {

    fun createTrials(): List<Trial> {
        ctcModel.checkModelForNewTrials(trialConfigModel.trials())
        ctcModel.checkModelForNewCohorts(trialConfigModel.cohorts())

        return trialConfigModel.trials().map { trialConfig ->
            val trialId = trialConfig.trialId
            val referencesById = trialConfigModel.referencesForTrial(trialId)
            ImmutableTrial.builder()
                .identification(toIdentification(trialConfig))
                .generalEligibility(toEligibility(trialConfigModel.generalInclusionCriteriaForTrial(trialId), referencesById))
                .cohorts(cohortsForTrial(trialId, referencesById))
                .build()
        }
    }

    private fun cohortsForTrial(trialId: String, referencesById: Map<String, InclusionCriteriaReferenceConfig>): List<Cohort> {
        return trialConfigModel.cohortsForTrial(trialId).map { cohortConfig ->
            val cohortId = cohortConfig.cohortId
            ImmutableCohort.builder()
                .metadata(ctcModel.resolveCohortMetadata(cohortConfig))
                .eligibility(toEligibility(trialConfigModel.specificInclusionCriteriaForCohort(trialId, cohortId), referencesById))
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

    private fun toIdentification(trialConfig: TrialDefinitionConfig): TrialIdentification {
        return ImmutableTrialIdentification.builder()
            .trialId(trialConfig.trialId)
            .open(determineOpenStatus(trialConfig))
            .acronym(trialConfig.acronym)
            .title(trialConfig.title)
            .build()
    }

    private fun determineOpenStatus(trialConfig: TrialDefinitionConfig): Boolean {
        val openInCTC: Boolean? = ctcModel.isTrialOpen(trialConfig)
        if (openInCTC != null) {
            return openInCTC
        }

        return trialConfig.open
            ?: throw java.lang.IllegalStateException(
                "Could not determine open status for trial, "
                        + "either from CTC or from manual config for '" + trialConfig.trialId + "'"
            )
    }

    companion object {
        @Throws(IOException::class)
        fun create(
            trialConfigDirectory: String,
            ctcModel: CTCModel,
            doidModel: DoidModel,
            geneFilter: GeneFilter,
            treatmentDatabase: TreatmentDatabase
        ): TrialFactory {
            val molecularInputChecker = MolecularInputChecker(geneFilter)
            val functionInputResolver = FunctionInputResolver(doidModel, molecularInputChecker, treatmentDatabase)
            val eligibilityFactory = EligibilityFactory(functionInputResolver)
            val trialConfigModel: TrialConfigModel = TrialConfigModel.create(trialConfigDirectory, eligibilityFactory)
            return TrialFactory(trialConfigModel, ctcModel, eligibilityFactory)
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