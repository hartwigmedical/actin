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

object RuleMappingResourcesTestFactory {

    fun create(
        doidModel: DoidModel = TestDoidModelFactory.createMinimalTestDoidModel(),
        icdModel: IcdModel = TestIcdFactory.createTestModel(),
        atcTree: AtcTree = AtcTestFactory.createProperAtcTree(),
        treatmentDatabase: TreatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    ): RuleMappingResources {
        return RuleMappingResources(
            referenceDateProvider = ReferenceDateProviderTestFactory.createCurrentDateProvider(),
            doidModel = doidModel,
            icdModel = icdModel,
            atcTree = atcTree,
            treatmentDatabase = treatmentDatabase,
            treatmentEfficacyPredictionJson = null,
            algoConfiguration = AlgoConfiguration()
        )
    }
}
