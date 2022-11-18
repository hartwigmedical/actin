package com.hartwig.actin.molecular.orange.evidence;

import java.util.List;

import com.hartwig.actin.molecular.orange.curation.ExternalTrialMapping;
import com.hartwig.actin.molecular.orange.evidence.actionable.ActionableEventMatcher;
import com.hartwig.actin.molecular.orange.evidence.actionable.ActionableEventMatcherFactory;
import com.hartwig.actin.molecular.orange.evidence.known.KnownEventResolver;
import com.hartwig.actin.molecular.orange.evidence.known.KnownEventResolverFactory;
import com.hartwig.actin.molecular.serve.KnownGene;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.KnownEvents;

import org.jetbrains.annotations.NotNull;

public final class EvidenceDatabaseFactory {

    private EvidenceDatabaseFactory() {
    }

    @NotNull
    public static EvidenceDatabase create(@NotNull KnownEvents knownEvents, @NotNull List<KnownGene> knownGenes,
            @NotNull ActionableEvents actionableEvents, @NotNull List<ExternalTrialMapping> externalTrialMappings) {
        KnownEventResolver knownEventResolver = KnownEventResolverFactory.create(knownEvents, knownGenes);

        ActionableEventMatcherFactory factory = new ActionableEventMatcherFactory(externalTrialMappings);
        ActionableEventMatcher actionableEventMatcher = factory.create(actionableEvents);

        return new EvidenceDatabase(knownEventResolver, actionableEventMatcher);
    }
}
