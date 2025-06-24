package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.calendar.ReferenceDateProviderTestFactory
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.configuration.AlgoConfiguration
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.TestIcdFactory
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.trial.input.TestFunctionInputResolverFactory

object RuleMappingResourcesTestFactory {

    fun create(
        doidModel: DoidModel = TestDoidModelFactory.createMinimalTestDoidModel(),
        icdModel: IcdModel = TestIcdFactory.createTestModel(),
        atcTree: AtcTree = AtcTestFactory.createProperAtcTree(),
        treatmentDatabase: TreatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    ): RuleMappingResources {
        val functionInputResolver =
            TestFunctionInputResolverFactory.createResolverWithDoidModelAndTreatmentDatabase(doidModel, treatmentDatabase)
        return RuleMappingResources(
            referenceDateProvider = ReferenceDateProviderTestFactory.createCurrentDateProvider(),
            doidModel = doidModel,
            icdModel = icdModel,
            functionInputResolver = functionInputResolver,
            atcTree = atcTree,
            treatmentDatabase = treatmentDatabase,
            personalizationDataPath = null,
            treatmentEfficacyPredictionJson = null,
            algoConfiguration = AlgoConfiguration(),
            maxMolecularTestAge = null
        )
    }
}