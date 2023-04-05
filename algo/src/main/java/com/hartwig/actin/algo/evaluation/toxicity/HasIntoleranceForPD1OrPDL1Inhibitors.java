package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.algo.othercondition.OtherConditionSelector;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasIntoleranceForPD1OrPDL1Inhibitors implements EvaluationFunction {

    static final List<String> INTOLERANCE_TERMS =
            List.of("Pembrolizumab", "Nivolumab", "Cemiplimab", "Avelumab", "Atezolizumab", "Durvalumab", "PD-1", "PD-L1");

    @NotNull
    private final DoidModel doidModel;

    HasIntoleranceForPD1OrPDL1Inhibitors(@NotNull DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> intolerances = record.clinical()
                .intolerances()
                .stream()
                .map(Intolerance::name)
                .filter(name -> stringCaseInsensitivelyMatchesQueryCollection(name, INTOLERANCE_TERMS))
                .collect(Collectors.toSet());

        if (!intolerances.isEmpty()) {
            return EvaluationFactory.pass("Patient has PD-1/PD-L1 intolerance(s) " + Format.concat(intolerances),
                    "Patient has intolerance(s)");
        } else {
            Set<String> autoImmuneDiseaseTerms = OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())
                    .stream()
                    .flatMap(priorOtherCondition -> priorOtherCondition.doids().stream())
                    .filter(doid -> doidModel.doidWithParents(doid).contains(DoidConstants.AUTOIMMUNE_DISEASE_DOID))
                    .map(doidModel::resolveTermForDoid)
                    .collect(Collectors.toSet());

            if (!autoImmuneDiseaseTerms.isEmpty()) {
                return EvaluationFactory.warn("Patient has autoimmune disease condition(s) " + Format.concat(autoImmuneDiseaseTerms)
                        + " which may indicate intolerance for immunotherapy", "Patient may have intolerance");
            } else {
                return EvaluationFactory.fail("Patient does not have PD-1/PD-L1 intolerance", "Patient does not have intolerance");
            }
        }
    }
}
