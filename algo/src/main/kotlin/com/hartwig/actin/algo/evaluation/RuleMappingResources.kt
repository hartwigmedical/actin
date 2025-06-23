package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.configuration.AlgoConfiguration
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.trial.input.FunctionInputResolver
import java.time.LocalDate

data class RuleMappingResources(
    val referenceDateProvider: ReferenceDateProvider,
    val doidModel: DoidModel,
    val icdModel: IcdModel,
    val functionInputResolver: FunctionInputResolver,
    val atcTree: AtcTree,
    val treatmentDatabase: TreatmentDatabase,
    val personalizationDataPath: String?,
    val treatmentEfficacyPredictionJson: String?,
    val algoConfiguration: AlgoConfiguration,
    val maxMolecularTestAge: LocalDate?
)