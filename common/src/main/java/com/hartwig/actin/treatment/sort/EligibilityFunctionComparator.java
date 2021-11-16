package com.hartwig.actin.treatment.sort;

import java.util.Comparator;
import java.util.List;

import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.jetbrains.annotations.NotNull;

public class EligibilityFunctionComparator implements Comparator<EligibilityFunction> {

    private static final Comparator<EligibilityFunction> INSTANCE = new EligibilityFunctionComparator();

    @Override
    public int compare(@NotNull EligibilityFunction function1, @NotNull EligibilityFunction function2) {
        int ruleCompare = function1.rule().toString().compareTo(function2.rule().toString());
        return ruleCompare != 0 ? ruleCompare : paramCompare(function1.parameters(), function2.parameters());
    }

    private static int paramCompare(@NotNull List<Object> parameters1, @NotNull List<Object> parameters2) {
        if (parameters1.isEmpty() && parameters2.isEmpty()) {
            return 0;
        } else if (parameters1.isEmpty()) {
            return 1;
        } else if (parameters2.isEmpty()) {
            return -1;
        }
        int sizeCompare = parameters1.size() - parameters2.size();
        if (sizeCompare == 0) {
            for (int i = 0; i < parameters1.size(); i++) {
                Object object1 = parameters1.get(i);
                Object object2 = parameters2.get(i);
                if (object1 instanceof EligibilityFunction && object2 instanceof EligibilityFunction) {
                    int functionCompare = INSTANCE.compare((EligibilityFunction) object1, (EligibilityFunction) object2);
                    if (functionCompare != 0) {
                        return functionCompare;
                    }
                } else if (object1 instanceof String && object2 instanceof String) {
                    int stringCompare = ((String) object1).compareTo((String) object2);
                    if (stringCompare != 0) {
                        return 0;
                    }
                } else {
                    // Assume parameters can be either strings or eligibility functions
                    return object1 instanceof EligibilityFunction ? 1 : -1;
                }
            }
            return 0;
        } else {
            return sizeCompare;
        }
    }
}
