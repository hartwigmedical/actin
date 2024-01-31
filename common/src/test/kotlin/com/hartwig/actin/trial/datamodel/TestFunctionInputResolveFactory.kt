package com.hartwig.actin.trial.datamodel

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver

object TestFunctionInputResolveFactory {
    fun createTestResolver(): FunctionInputResolver {
        return createResolverWithDoidModel(TestDoidModelFactory.createMinimalTestDoidModel())
    }

    fun createResolverWithDoidAndTerm(doid: String, term: String): FunctionInputResolver {
        return createResolverWithDoidModel(TestDoidModelFactory.createWithOneDoidAndTerm(doid, term))
    }

    fun createResolverWithOneValidGene(gene: String): FunctionInputResolver {
        return FunctionInputResolver(
            TestDoidModelFactory.createMinimalTestDoidModel(),
            MolecularInputChecker(TestGeneFilterFactory.createValidForGenes(gene)),
            TreatmentDatabase(emptyMap(), emptyMap())
        )
    }

    fun createResolverWithDoidModel(doidModel: DoidModel): FunctionInputResolver {
        return FunctionInputResolver(
            doidModel,
            MolecularInputChecker.createAnyGeneValid(),
            TestTreatmentDatabaseFactory.createProper()
        )
    }
}
