package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.evaluation.bloodtransfusion.BloodTransfusionRuleMapper
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionRuleMapper
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityRuleMapper
import com.hartwig.actin.algo.evaluation.general.GeneralRuleMapper
import com.hartwig.actin.algo.evaluation.infection.InfectionRuleMapper
import com.hartwig.actin.algo.evaluation.laboratory.LaboratoryRuleMapper
import com.hartwig.actin.algo.evaluation.medication.MedicationRuleMapper
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleMapper
import com.hartwig.actin.algo.evaluation.priortumor.PreviousTumorRuleMapper
import com.hartwig.actin.algo.evaluation.reproduction.ReproductionRuleMapper
import com.hartwig.actin.algo.evaluation.surgery.SurgeryRuleMapper
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityRuleMapper
import com.hartwig.actin.algo.evaluation.treatment.TreatmentRuleMapper
import com.hartwig.actin.algo.evaluation.tumor.TumorRuleMapper
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionRuleMapper
import com.hartwig.actin.algo.evaluation.washout.WashoutRuleMapper
import com.hartwig.actin.trial.input.EligibilityRule

object FunctionCreatorFactory {

    fun create(resources: RuleMappingResources): Map<EligibilityRule, FunctionCreator> {
        return listOf(
            BloodTransfusionRuleMapper(resources),
            CardiacFunctionRuleMapper(resources),
            GeneralRuleMapper(resources),
            InfectionRuleMapper(resources),
            LaboratoryRuleMapper(resources),
            MedicationRuleMapper(resources),
            MolecularRuleMapper(resources),
            ComorbidityRuleMapper(resources),
            PreviousTumorRuleMapper(resources),
            ReproductionRuleMapper(resources),
            SurgeryRuleMapper(resources),
            ToxicityRuleMapper(resources),
            TreatmentRuleMapper(resources),
            TumorRuleMapper(resources),
            VitalFunctionRuleMapper(resources),
            WashoutRuleMapper(resources)
        ).fold(emptyMap()) { acc, mapper -> acc + mapper.createMappings() }
    }
}