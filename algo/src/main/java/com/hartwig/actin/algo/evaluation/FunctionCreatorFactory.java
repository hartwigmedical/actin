package com.hartwig.actin.algo.evaluation;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.bloodpressure.BloodPressureRuleMapping;
import com.hartwig.actin.algo.evaluation.bloodtransfusion.BloodTransfusionRuleMapping;
import com.hartwig.actin.algo.evaluation.general.GeneralRuleMapping;
import com.hartwig.actin.algo.evaluation.hospital.HospitalRuleMapping;
import com.hartwig.actin.algo.evaluation.infection.InfectionRuleMapping;
import com.hartwig.actin.algo.evaluation.laboratory.LaboratoryRuleMapping;
import com.hartwig.actin.algo.evaluation.medication.MedicationRuleMapping;
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleMapping;
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionRuleMapping;
import com.hartwig.actin.algo.evaluation.pregnancy.PregnancyRuleMapping;
import com.hartwig.actin.algo.evaluation.surgery.SurgeryRuleMapping;
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityRuleMapping;
import com.hartwig.actin.algo.evaluation.treatment.TreatmentRuleMapping;
import com.hartwig.actin.algo.evaluation.trialparticipation.TrialParticipationRuleMapping;
import com.hartwig.actin.algo.evaluation.tumor.TumorRuleMapping;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

final class FunctionCreatorFactory {

    private FunctionCreatorFactory() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> createFunctionCreatorMap(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.putAll(GeneralRuleMapping.create());
        map.putAll(TumorRuleMapping.create(doidModel));
        map.putAll(TreatmentRuleMapping.create());
        map.putAll(MolecularRuleMapping.create());
        map.putAll(LaboratoryRuleMapping.create());
        map.putAll(OtherConditionRuleMapping.create(doidModel));
        map.putAll(InfectionRuleMapping.create());
        map.putAll(MedicationRuleMapping.create());
        map.putAll(PregnancyRuleMapping.create());
        map.putAll(ToxicityRuleMapping.create());
        map.putAll(BloodPressureRuleMapping.create());
        map.putAll(BloodTransfusionRuleMapping.create());
        map.putAll(SurgeryRuleMapping.create());
        map.putAll(TrialParticipationRuleMapping.create());
        map.putAll(HospitalRuleMapping.create());

        return map;
    }
}
