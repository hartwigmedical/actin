package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.doid.CuppaToDoidMapping
import com.hartwig.actin.configuration.AlgoConfiguration
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.medication.AtcTree

data class RuleMappingResources(
    val referenceDateProvider: ReferenceDateProvider,
    val doidModel: DoidModel,
    val cuppaToDoidMapping: CuppaToDoidMapping,
    val icdModel: IcdModel,
    val atcTree: AtcTree,
    val treatmentDatabase: TreatmentDatabase,
    val treatmentEfficacyPredictionJson: String?,
    val algoConfiguration: AlgoConfiguration
)
