package com.hartwig.actin.treatment.trial;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker;
import com.hartwig.actin.treatment.ctc.CTCModel;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.CriterionReference;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.ImmutableCohort;
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibility;
import com.hartwig.actin.treatment.datamodel.ImmutableTrial;
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;
import com.hartwig.actin.treatment.input.FunctionInputResolver;
import com.hartwig.actin.treatment.sort.CohortComparator;
import com.hartwig.actin.treatment.sort.CriterionReferenceComparator;
import com.hartwig.actin.treatment.sort.EligibilityComparator;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaReferenceConfig;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.jetbrains.annotations.NotNull;

public class TrialFactory {

    @NotNull
    private final TrialConfigModel trialConfigModel;
    @NotNull
    private final CTCModel ctcModel;
    @NotNull
    private final EligibilityFactory eligibilityFactory;

    @NotNull
    public static TrialFactory create(@NotNull String trialConfigDirectory, @NotNull CTCModel ctcModel, @NotNull DoidModel doidModel,
            @NotNull GeneFilter geneFilter) throws IOException {
        MolecularInputChecker molecularInputChecker = new MolecularInputChecker(geneFilter);
        FunctionInputResolver functionInputResolver = new FunctionInputResolver(doidModel, molecularInputChecker);
        EligibilityFactory eligibilityFactory = new EligibilityFactory(functionInputResolver);

        TrialConfigModel trialConfigModel = TrialConfigModel.create(trialConfigDirectory, eligibilityFactory);

        return new TrialFactory(trialConfigModel, ctcModel, eligibilityFactory);
    }

    @VisibleForTesting
    TrialFactory(@NotNull TrialConfigModel trialConfigModel, @NotNull CTCModel ctcModel, @NotNull EligibilityFactory eligibilityFactory) {
        this.trialConfigModel = trialConfigModel;
        this.ctcModel = ctcModel;
        this.eligibilityFactory = eligibilityFactory;
    }

    @NotNull
    public List<Trial> create() {
        List<Trial> trials = Lists.newArrayList();

        for (TrialDefinitionConfig trialConfig : trialConfigModel.trials()) {
            String trialId = trialConfig.trialId();

            List<InclusionCriteriaReferenceConfig> references = trialConfigModel.referencesForTrial(trialId);
            trials.add(ImmutableTrial.builder()
                    .identification(toIdentification(trialConfig))
                    .generalEligibility(toEligibility(trialConfigModel.generalInclusionCriteriaForTrial(trialId), references))
                    .cohorts(cohortsForTrial(trialId, references))
                    .build());
        }

        return trials;
    }

    @NotNull
    private TrialIdentification toIdentification(@NotNull TrialDefinitionConfig trialConfig) {
        return ImmutableTrialIdentification.builder()
                .trialId(trialConfig.trialId())
                .open(ctcModel.isTrialOpen(trialConfig))
                .acronym(trialConfig.acronym())
                .title(trialConfig.title())
                .build();
    }

    @NotNull
    private List<Cohort> cohortsForTrial(@NotNull String trialId, @NotNull List<InclusionCriteriaReferenceConfig> references) {
        List<Cohort> cohorts = Lists.newArrayList();

        for (CohortDefinitionConfig cohortConfig : trialConfigModel.cohortsForTrial(trialId)) {
            String cohortId = cohortConfig.cohortId();
            cohorts.add(ImmutableCohort.builder()
                    .metadata(ctcModel.resolveForCohort(cohortConfig))
                    .eligibility(toEligibility(trialConfigModel.specificInclusionCriteriaForCohort(trialId, cohortId), references))
                    .build());
        }

        cohorts.sort(new CohortComparator());

        return cohorts;
    }

    @NotNull
    private List<Eligibility> toEligibility(@NotNull List<InclusionCriteriaConfig> criteria,
            @NotNull List<InclusionCriteriaReferenceConfig> references) {
        List<Eligibility> eligibility = Lists.newArrayList();

        for (InclusionCriteriaConfig criterion : criteria) {
            eligibility.add(ImmutableEligibility.builder()
                    .references(resolveReferences(references, criterion.referenceIds()))
                    .function(eligibilityFactory.generateEligibilityFunction(criterion.inclusionRule()))
                    .build());
        }

        eligibility.sort(new EligibilityComparator());

        return eligibility;
    }

    @NotNull
    private static Set<CriterionReference> resolveReferences(@NotNull List<InclusionCriteriaReferenceConfig> configs,
            @NotNull Set<String> referenceIds) {
        Set<CriterionReference> references = Sets.newTreeSet(new CriterionReferenceComparator());

        for (String referenceId : referenceIds) {
            InclusionCriteriaReferenceConfig config = findReferenceConfig(configs, referenceId);
            references.add(ImmutableCriterionReference.builder().id(config.referenceId()).text(config.referenceText()).build());
        }

        return references;
    }

    @NotNull
    private static InclusionCriteriaReferenceConfig findReferenceConfig(@NotNull List<InclusionCriteriaReferenceConfig> configs,
            @NotNull String referenceId) {
        for (InclusionCriteriaReferenceConfig config : configs) {
            if (config.referenceId().equals(referenceId)) {
                return config;
            }
        }

        // Should not happen in a valid trial config database.
        throw new IllegalStateException("No config found for reference with ID: " + referenceId);
    }
}
