package com.hartwig.actin.algo.evaluation;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.evaluation.bloodtransfusion.BloodTransfusionRuleMapper;
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionRuleMapper;
import com.hartwig.actin.algo.evaluation.complication.ComplicationRuleMapper;
import com.hartwig.actin.algo.evaluation.general.GeneralRuleMapper;
import com.hartwig.actin.algo.evaluation.infection.InfectionRuleMapper;
import com.hartwig.actin.algo.evaluation.laboratory.LaboratoryRuleMapper;
import com.hartwig.actin.algo.evaluation.lifestyle.LifestyleRuleMapper;
import com.hartwig.actin.algo.evaluation.medication.MedicationRuleMapper;
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleMapping;
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionRuleMapping;
import com.hartwig.actin.algo.evaluation.priortumor.PreviousTumorRuleMapping;
import com.hartwig.actin.algo.evaluation.reproduction.ReproductionRuleMapping;
import com.hartwig.actin.algo.evaluation.surgery.SurgeryRuleMapping;
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityRuleMapping;
import com.hartwig.actin.algo.evaluation.treatment.TreatmentRuleMapping;
import com.hartwig.actin.algo.evaluation.tumor.TumorRuleMapping;
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionRuleMapping;
import com.hartwig.actin.algo.evaluation.washout.WashoutRuleMapping;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

final class FunctionCreatorFactory {

    private FunctionCreatorFactory() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        // TODO
        RuleMappingResources resources = null;

        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.putAll(GeneralRuleMapper.create(referenceDateProvider));
        map.putAll(TumorRuleMapping.create(doidModel));
        map.putAll(TreatmentRuleMapping.create(referenceDateProvider));
        map.putAll(PreviousTumorRuleMapping.create(doidModel, referenceDateProvider));
        map.putAll(MolecularRuleMapping.create());
        map.putAll(LaboratoryRuleMapper.create(referenceDateProvider));
        map.putAll(OtherConditionRuleMapping.create(doidModel, referenceDateProvider));
        map.putAll(CardiacFunctionRuleMapper.create(doidModel));
        map.putAll(InfectionRuleMapper.create(doidModel));
        map.putAll(MedicationRuleMapper.create(referenceDateProvider));
        map.putAll(WashoutRuleMapping.create(referenceDateProvider));
        map.putAll(ReproductionRuleMapping.create());
        map.putAll(ComplicationRuleMapper.create(referenceDateProvider));
        map.putAll(ToxicityRuleMapping.create(doidModel));
        map.putAll(VitalFunctionRuleMapping.create());
        map.putAll(new BloodTransfusionRuleMapper(resources).createMappings());
        map.putAll(SurgeryRuleMapping.create(referenceDateProvider));
        map.putAll(LifestyleRuleMapper.create());

        return map;
    }
}
