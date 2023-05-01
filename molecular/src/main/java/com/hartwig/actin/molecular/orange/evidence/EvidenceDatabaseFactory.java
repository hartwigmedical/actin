package com.hartwig.actin.molecular.orange.evidence;

import java.util.List;
import java.util.Set;

import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.DoidModelFactory;
import com.hartwig.actin.doid.datamodel.DoidEntry;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionableEventMatcher;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionableEventMatcherFactory;
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMapper;
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMapping;
import com.hartwig.actin.molecular.orange.evidence.known.KnownEventResolver;
import com.hartwig.actin.molecular.orange.evidence.known.KnownEventResolverFactory;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.KnownEvents;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EvidenceDatabaseFactory {

    private EvidenceDatabaseFactory() {
    }

    @NotNull
    public static EvidenceDatabase create(@NotNull KnownEvents knownEvents, @NotNull ActionableEvents actionableEvents,
            @NotNull List<ExternalTrialMapping> externalTrialMappings, @NotNull DoidEntry doidEntry, @Nullable Set<String> tumorDoids) {
        ExternalTrialMapper externalTrialMapper = new ExternalTrialMapper(externalTrialMappings);
        DoidModel doidModel = DoidModelFactory.createFromDoidEntry(doidEntry);
        ActionableEventMatcherFactory factory = new ActionableEventMatcherFactory(externalTrialMapper, doidModel, tumorDoids);
        ActionableEventMatcher actionableEventMatcher = factory.create(actionableEvents);

        KnownEventResolver knownEventResolver = KnownEventResolverFactory.create(knownEvents);

        return new EvidenceDatabase(knownEventResolver, actionableEventMatcher);
    }
}
