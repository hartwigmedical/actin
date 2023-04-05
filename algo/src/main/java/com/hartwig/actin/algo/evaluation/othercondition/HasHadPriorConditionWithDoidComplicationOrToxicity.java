package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.Characteristic;
import static com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.failGeneral;
import static com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.failSpecific;
import static com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.passGeneral;
import static com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.passSpecific;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.complication.ComplicationFunctions;
import com.hartwig.actin.algo.othercondition.OtherConditionSelector;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasHadPriorConditionWithDoidComplicationOrToxicity implements EvaluationFunction {

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final String doidToFind;
    @NotNull
    private final String complicationCategoryToFind;
    @NotNull
    private final String toxicityCategoryToFind;

    HasHadPriorConditionWithDoidComplicationOrToxicity(@NotNull DoidModel doidModel, @NotNull final String doidToFind,
            @NotNull String complicationCategoryToFind, @NotNull String toxicityToFind) {
        this.doidModel = doidModel;
        this.doidToFind = doidToFind;
        this.complicationCategoryToFind = complicationCategoryToFind;
        this.toxicityCategoryToFind = toxicityToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        String doidTerm = Optional.ofNullable(doidModel.resolveTermForDoid(doidToFind)).orElse("unknown doid");

        Set<String> matchingConditions =
                OtherConditionSelector.selectConditionsMatchingDoid(record.clinical().priorOtherConditions(), doidToFind, doidModel);

        Set<String> matchingComplications = ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record,
                Collections.singletonList(complicationCategoryToFind));

        Set<String> matchingToxicities = record.clinical()
                .toxicities()
                .stream()
                .filter(toxicity -> Optional.ofNullable(toxicity.grade()).orElse(0) >= 2 || toxicity.source()
                        .equals(ToxicitySource.QUESTIONNAIRE))
                .filter(toxicity -> toxicity.categories()
                        .stream()
                        .map(String::toLowerCase)
                        .anyMatch(t -> t.contains(toxicityCategoryToFind.toLowerCase())))
                .map(Toxicity::name)
                .collect(Collectors.toSet());

        if (!matchingConditions.isEmpty() || !matchingComplications.isEmpty() || !matchingToxicities.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllPassSpecificMessages(passSpecificMessages(doidTerm,
                            matchingConditions,
                            matchingComplications,
                            matchingToxicities))
                    .addPassGeneralMessages(passGeneral(doidTerm))
                    .build();
        }

        return EvaluationFactory.fail(failSpecific(doidTerm), failGeneral());
    }

    @NotNull
    private static Collection<String> passSpecificMessages(String doidTerm, Set<String> matchingConditions,
            Set<String> matchingComplications, Set<String> matchingToxicities) {
        List<String> messages = new ArrayList<>();
        if (!matchingConditions.isEmpty()) {
            messages.add(passSpecific(Characteristic.CONDITION, matchingConditions, doidTerm));
        }
        if (!matchingComplications.isEmpty()) {
            messages.add(passSpecific(Characteristic.COMPLICATION, matchingComplications, doidTerm));
        }
        if (!matchingToxicities.isEmpty()) {
            messages.add(passSpecific(Characteristic.TOXICITY, matchingToxicities, doidTerm));
        }
        return Collections.unmodifiableCollection(messages);
    }

}
