package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.complication.ComplicationFunctions;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.algo.othercondition.OtherConditionSelector;
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
    private final String toxicityToFind;

    HasHadPriorConditionWithDoidComplicationOrToxicity(@NotNull DoidModel doidModel, @NotNull final String doidToFind,
            @NotNull String complicationCategoryToFind, @NotNull String toxicityToFind) {
        this.doidModel = doidModel;
        this.doidToFind = doidToFind;
        this.complicationCategoryToFind = complicationCategoryToFind;
        this.toxicityToFind = toxicityToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        String doidTerm = doidModel.resolveTermForDoid(doidToFind);

        Set<String> matchingConditions =
                OtherConditionSelector.selectConditionsMatchingDoid(record.clinical().priorOtherConditions(), doidToFind, doidModel);

        Set<String> matchingComplicationCategories = ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record,
                Collections.singletonList(complicationCategoryToFind));

        Set<String> matchingToxicities = record.clinical()
                .toxicities()
                .stream()
                .flatMap(toxicity -> toxicity.categories().stream())
                .filter(toxicityToFind::contains)
                .collect(Collectors.toSet());

        if (!matchingConditions.isEmpty() || !matchingComplicationCategories.isEmpty() || !matchingToxicities.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllPassSpecificMessages(passSpecificMessages(doidTerm,
                            matchingConditions,
                            matchingComplicationCategories,
                            matchingToxicities))
                    .addPassGeneralMessages("Present conditions" + Format.concat(matchingConditions))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no other condition belonging to category " + doidTerm + " or ")
                .addFailGeneralMessages("No relevant non-oncological condition")
                .build();
    }

    @NotNull
    private static Collection<String> passSpecificMessages(String doidTerm, Set<String> matchingConditions,
            Set<String> matchingComplicationCategories, Set<String> matchingToxicityCategories) {
        List<String> messages = new ArrayList<>();
        if (!matchingConditions.isEmpty()) {
            messages.add("Patient has conditions" + Format.concat(matchingConditions) + ", which belong(s) to category " + doidTerm);
        }
        if (!matchingComplicationCategories.isEmpty()) {
            messages.add("Patient has had complications of categories " + Format.concat(matchingConditions));
        }
        if (!matchingToxicityCategories.isEmpty()) {
            messages.add("Patient has toxicities of categories" + Format.concat(matchingConditions));
        }
        return Collections.unmodifiableCollection(messages);
    }

}
