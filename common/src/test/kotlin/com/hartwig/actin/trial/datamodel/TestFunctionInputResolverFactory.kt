package com.hartwig.actin.trial.datamodel

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver

const val ATC_CODE_1 = "code1"
const val ATC_CODE_2 = "code2"
const val CATEGORY_1 = "category1"
const val CATEGORY_2 = "category2"

object TestFunctionInputResolverFactory {
    private val medicationCategories =
        MedicationCategories(emptyMap(), AtcTree(mapOf(ATC_CODE_1 to CATEGORY_1, ATC_CODE_2 to CATEGORY_2)))

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
            TreatmentDatabase(emptyMap(), emptyMap()),
            medicationCategories
        )
    }

    fun createResolverWithDoidModelAndTreatmentDatabase(doidModel: DoidModel, treatmentDatabase: TreatmentDatabase): FunctionInputResolver {
        return FunctionInputResolver(doidModel, MolecularInputChecker.createAnyGeneValid(), treatmentDatabase, medicationCategories)
    }

    private fun createResolverWithDoidModel(doidModel: DoidModel): FunctionInputResolver {
        return createResolverWithDoidModelAndTreatmentDatabase(doidModel, TestTreatmentDatabaseFactory.createProper())
    }
}
