package com.hartwig.actin.treatment.datamodel;

import java.io.IOException;
import java.util.Collections;

import com.google.common.io.Resources;
import com.hartwig.actin.TreatmentDatabase;
import com.hartwig.actin.TreatmentDatabaseFactory;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

public final class TestFunctionInputResolveFactory {

    private TestFunctionInputResolveFactory() {
    }

    @NotNull
    public static FunctionInputResolver createTestResolver() {
        return createResolverWithDoidModel(TestDoidModelFactory.createMinimalTestDoidModel());
    }

    @NotNull
    public static FunctionInputResolver createResolverWithDoidAndTerm(@NotNull String doid, @NotNull String term) {
        return createResolverWithDoidModel(TestDoidModelFactory.createWithOneDoidAndTerm(doid, term));
    }

    @NotNull
    public static FunctionInputResolver createResolverWithOneValidGene(@NotNull String gene) {
        return new FunctionInputResolver(TestDoidModelFactory.createMinimalTestDoidModel(),
                new MolecularInputChecker(TestGeneFilterFactory.createValidForGenes(gene)),
                new TreatmentDatabase(Collections.emptyMap(), Collections.emptyMap()));
    }

    @NotNull
    public static FunctionInputResolver createResolverWithDoidModel(@NotNull DoidModel doidModel) {
        TreatmentDatabase treatmentDb;
        try {
            treatmentDb = TreatmentDatabaseFactory.createFromPath(Resources.getResource("clinical").getPath());
        } catch (IOException e) {
            LogManager.getLogger(TestFunctionInputResolveFactory.class)
                    .warn("Proceeding with empty treatment DB after failure to load from file: " + e.getMessage());
            treatmentDb = new TreatmentDatabase(Collections.emptyMap(), Collections.emptyMap());
        }
        return new FunctionInputResolver(doidModel, MolecularInputChecker.createAnyGeneValid(), treatmentDb);
    }
}
