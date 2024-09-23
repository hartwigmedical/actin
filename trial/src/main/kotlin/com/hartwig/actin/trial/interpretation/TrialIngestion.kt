package com.hartwig.actin.trial.interpretation

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.TrialIngestionResult
import com.hartwig.actin.trial.TrialIngestionStatus
import com.hartwig.actin.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.trial.config.TrialConfigModel
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.actin.trial.sort.CohortComparator
import com.hartwig.actin.trial.sort.CriterionReferenceComparator
import com.hartwig.actin.trial.sort.EligibilityComparator
import com.hartwig.actin.trial.status.TrialStatusConfigInterpreter

class TrialIngestion(
    private val trialConfigModel: TrialConfigModel,
    private val trialStatusConfigInterpreter: TrialStatusConfigInterpreter,
    private val eligibilityFactory: EligibilityFactory
) {

    fun ingestTrials(): TrialIngestionResult {
        trialStatusConfigInterpreter.checkModelForUnusedStudyMETCsToIgnore()
        trialStatusConfigInterpreter.checkModelForUnusedUnmappedCohortIds()
        trialStatusConfigInterpreter.checkModelForNewTrials(trialConfigModel.trials())
        trialStatusConfigInterpreter.checkModelForNewCohorts(trialConfigModel.cohorts())
        trialStatusConfigInterpreter.checkModelForUnusedStudiesNotInTrialStatusDatabase(trialConfigModel.trials())

        val trials = if (!trialConfigModel.validation().hasErrors()) createTrials() else emptyList()

        val trialStatusDatabaseValidation = trialStatusConfigInterpreter.validation()
        val trialConfigDatabaseValidation = trialStatusConfigInterpreter.appendTrialConfigValidation(trialConfigModel.validation())
        val unusedRules = EligibilityRuleUsageEvaluator.evaluate(trials, trialConfigModel.unusedRulesToKeep).map { it.name }.toSet()

        return TrialIngestionResult(
            TrialIngestionStatus.from(trialConfigDatabaseValidation, trialStatusDatabaseValidation, unusedRules),
            trialConfigDatabaseValidation,
            trialStatusDatabaseValidation,
            unusedRules,
            trials,
        )
    }

    private fun createTrials(): List<Trial> {
        val trials = trialConfigModel.trials().map { trialConfig ->
            val trialId = trialConfig.trialId
            val referencesById = trialConfigModel.referencesForTrial(trialId)
            Trial(
                identification = toIdentification(trialConfig),
                generalEligibility = toEligibility(trialConfigModel.generalInclusionCriteriaForTrial(trialId), referencesById),
                cohorts = cohortsForTrial(trialId, referencesById)
            )
        }
        return trials
    }

    private fun cohortsForTrial(trialId: String, referencesById: Map<String, InclusionCriteriaReferenceConfig>): List<Cohort> {
        return trialConfigModel.cohortsForTrial(trialId).map { cohortConfig ->
            val cohortId = cohortConfig.cohortId
            Cohort(
                metadata = trialStatusConfigInterpreter.resolveCohortMetadata(cohortConfig),
                eligibility = toEligibility(trialConfigModel.specificInclusionCriteriaForCohort(trialId, cohortId), referencesById)
            )
        }
            .sortedWith(CohortComparator())
    }

    private fun toEligibility(
        criteria: List<InclusionCriteriaConfig>,
        referencesById: Map<String, InclusionCriteriaReferenceConfig>
    ): List<Eligibility> {
        return criteria.map { criterion ->
            Eligibility(
                references = resolveReferences(referencesById, criterion.referenceIds).toSet(),
                function = eligibilityFactory.generateEligibilityFunction(criterion.inclusionRule)
            )
        }
            .sortedWith(EligibilityComparator())
    }

    private fun toIdentification(trialConfig: TrialDefinitionConfig): TrialIdentification {
        return TrialIdentification(
            trialId = trialConfig.trialId,
            open = determineOpenStatus(trialConfig),
            acronym = trialConfig.acronym,
            title = trialConfig.title,
            nctId = trialConfig.nctId,
            phase = trialConfig.phase?.let(TrialPhase::fromString)
        )
    }

    private fun determineOpenStatus(trialConfig: TrialDefinitionConfig): Boolean {
        val openInCTC: Boolean? = trialStatusConfigInterpreter.isTrialOpen(trialConfig)
        if (openInCTC != null) {
            return openInCTC
        }

        return trialConfig.open ?: throw java.lang.IllegalStateException(
            "Could not determine open status for trial, either from CTC or from manual config for '${trialConfig.trialId}'"
        )
    }

    companion object {

        fun create(
            trialConfigDirectory: String,
            configInterpreter: TrialStatusConfigInterpreter,
            doidModel: DoidModel,
            geneFilter: GeneFilter,
            treatmentDatabase: TreatmentDatabase,
            medicationCategories: MedicationCategories
        ): TrialIngestion {
            val functionInputResolver = FunctionInputResolver(
                doidModel, MolecularInputChecker(geneFilter), treatmentDatabase, medicationCategories
            )
            val eligibilityFactory = EligibilityFactory(functionInputResolver)
            val trialConfigModel = TrialConfigModel.create(trialConfigDirectory, eligibilityFactory)
            return TrialIngestion(trialConfigModel, configInterpreter, eligibilityFactory)
        }

        private fun resolveReferences(
            referencesById: Map<String, InclusionCriteriaReferenceConfig>,
            referenceIds: Set<String>
        ): List<CriterionReference> {
            return referenceIds.map { referenceId ->
                if (!referencesById.contains(referenceId)) {
                    throw IllegalStateException("No config found for reference with ID: $referenceId")
                }
                CriterionReference(id = referenceId, text = referencesById[referenceId]!!.referenceText)
            }
                .sortedWith(CriterionReferenceComparator())
        }
    }
}