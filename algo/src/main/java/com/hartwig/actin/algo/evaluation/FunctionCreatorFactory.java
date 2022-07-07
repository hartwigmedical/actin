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
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleMapper;
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionRuleMapper;
import com.hartwig.actin.algo.evaluation.priortumor.PreviousTumorRuleMapper;
import com.hartwig.actin.algo.evaluation.reproduction.ReproductionRuleMapper;
import com.hartwig.actin.algo.evaluation.surgery.SurgeryRuleMapper;
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityRuleMapper;
import com.hartwig.actin.algo.evaluation.treatment.TreatmentRuleMapper;
import com.hartwig.actin.algo.evaluation.tumor.TumorRuleMapper;
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionRuleMapper;
import com.hartwig.actin.algo.evaluation.washout.WashoutRuleMapper;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

final class FunctionCreatorFactory {

    private FunctionCreatorFactory() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        RuleMappingResources resources = ImmutableRuleMappingResources.builder()
                .referenceDateProvider(referenceDateProvider)
                .doidModel(doidModel)
                .functionInputResolver(new FunctionInputResolver(doidModel))
                .build();

        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.putAll(new GeneralRuleMapper(resources).createMappings());
        map.putAll(new TumorRuleMapper(resources).createMappings());
        map.putAll(new TreatmentRuleMapper(resources).createMappings());
        map.putAll(new PreviousTumorRuleMapper(resources).createMappings());
        map.putAll(new MolecularRuleMapper(resources).createMappings());
        map.putAll(new LaboratoryRuleMapper(resources).createMappings());
        map.putAll(new OtherConditionRuleMapper(resources).createMappings());
        map.putAll(new CardiacFunctionRuleMapper(resources).createMappings());
        map.putAll(new InfectionRuleMapper(resources).createMappings());
        map.putAll(new MedicationRuleMapper(resources).createMappings());
        map.putAll(new WashoutRuleMapper(resources).createMappings());
        map.putAll(new ReproductionRuleMapper(resources).createMappings());
        map.putAll(new ComplicationRuleMapper(resources).createMappings());
        map.putAll(new ToxicityRuleMapper(resources).createMappings());
        map.putAll(new VitalFunctionRuleMapper(resources).createMappings());
        map.putAll(new BloodTransfusionRuleMapper(resources).createMappings());
        map.putAll(new SurgeryRuleMapper(resources).createMappings());
        map.putAll(new LifestyleRuleMapper(resources).createMappings());

        return map;
    }
}
