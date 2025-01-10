package com.hartwig.actin.trial.input

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.TestIcdFactory
import com.hartwig.actin.icd.datamodel.IcdNode
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker

const val ATC_CODE_1 = "code1"
const val ATC_CODE_2 = "code2"
const val CATEGORY_1 = "category1"
const val CATEGORY_2 = "category2"

object TestFunctionInputResolverFactory {

    private val medicationCategories =
        MedicationCategories(emptyMap(), AtcTree(mapOf(ATC_CODE_1 to CATEGORY_1, ATC_CODE_2 to CATEGORY_2)))

    fun createTestResolver(
        doidModel: DoidModel = TestDoidModelFactory.createMinimalTestDoidModel(),
        icdModel: IcdModel = TestIcdFactory.createTestModel(),
        molecularInputChecker: MolecularInputChecker = MolecularInputChecker.createAnyGeneValid(),
        treatmentDatabase: TreatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    ): FunctionInputResolver {
        return FunctionInputResolver(doidModel, icdModel, molecularInputChecker, treatmentDatabase, medicationCategories)
    }

    fun createResolverWithDoidAndTerm(doid: String, term: String): FunctionInputResolver {
        return createResolverWithDoidModel(TestDoidModelFactory.createWithOneDoidAndTerm(doid, term))
    }

    fun createResolverWithTwoDoidsAndTerms(doids: List<String>, terms: List<String>): FunctionInputResolver {
        return createResolverWithDoidModel(TestDoidModelFactory.createWithTwoDoidsAndTerms(doids, terms))
    }

    fun createResolverWithIcdNodes(nodes: List<IcdNode>): FunctionInputResolver = createTestResolver(icdModel = IcdModel.create(nodes))

    fun createResolverWithOneValidGene(gene: String): FunctionInputResolver =
        createTestResolver(molecularInputChecker = MolecularInputChecker(TestGeneFilterFactory.createValidForGenes(gene)))

    fun createResolverWithDoidModelAndTreatmentDatabase(doidModel: DoidModel, treatmentDatabase: TreatmentDatabase): FunctionInputResolver =
        createTestResolver(doidModel = doidModel, treatmentDatabase = treatmentDatabase)

    private fun createResolverWithDoidModel(doidModel: DoidModel): FunctionInputResolver = createTestResolver(doidModel = doidModel)

}
