package com.hartwig.actin.algo.evaluation.laboratory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class CreatinineFunctions {

    private static final double DEFAULT_MIN_WEIGHT_FEMALE = 50D;
    private static final double DEFAULT_MIN_WEIGHT_MALE = 65D;

    private CreatinineFunctions() {
    }

    @NotNull
    public static List<Double> calcMDRD(int birthYear, int referenceYear, @NotNull Gender gender, @NotNull LabValue creatinine) {
        List<Double> mdrdValues = Lists.newArrayList();

        int age = referenceYear - birthYear;

        double base = 175 * Math.pow(creatinine.value() / 88.4, -1.154) * Math.pow(age, -0.203);

        double adjusted = base;
        if (gender == Gender.FEMALE) {
            adjusted = base * 0.742;
        }

        mdrdValues.add(adjusted);
        mdrdValues.add(adjusted * 1.212);

        return mdrdValues;
    }

    @NotNull
    public static List<Double> calcCKDEPI(int birthYear, int referenceYear, @NotNull Gender gender, @NotNull LabValue creatinine) {
        List<Double> ckdepiValues = Lists.newArrayList();

        int age = referenceYear - birthYear;

        boolean isFemale = gender == Gender.FEMALE;
        double correction = isFemale ? 61.9 : 79.6;
        double power = isFemale ? -0.329 : -0.411;

        double factor1 = Math.pow(Math.min(creatinine.value() / correction, 1), power);
        double factor2 = Math.pow(Math.max(creatinine.value() / correction, 1), -1.209);

        double base = 141 * factor1 * factor2 * Math.pow(0.993, age);

        double adjusted = base;
        if (isFemale) {
            adjusted = base * 1.018;
        }

        ckdepiValues.add(adjusted);
        ckdepiValues.add(adjusted * 1.159);

        return ckdepiValues;
    }

    @NotNull
    public static EvaluationResult interpretEGFREvaluations(@NotNull Set<EvaluationResult> evaluations) {
        if (evaluations.contains(EvaluationResult.FAIL)) {
            return evaluations.contains(EvaluationResult.PASS) ? EvaluationResult.UNDETERMINED : EvaluationResult.FAIL;
        } else if (evaluations.contains(EvaluationResult.UNDETERMINED)) {
            return EvaluationResult.UNDETERMINED;
        } else {
            return  EvaluationResult.PASS;
        }
    }

    public static double calcCockcroftGault(int birthYear, int referenceYear, @NotNull Gender gender, @Nullable Double weight,
            @NotNull LabValue creatinine) {
        boolean isFemale = gender == Gender.FEMALE;

        double effectiveWeight =
                Objects.requireNonNullElse(weight, isFemale ? DEFAULT_MIN_WEIGHT_FEMALE : DEFAULT_MIN_WEIGHT_MALE);

        int age = referenceYear - birthYear;

        double base = (140 - age) * effectiveWeight / (0.81 * creatinine.value());

        return isFemale ? base * 0.85 : base;
    }

    @Nullable
    public static Double determineWeight(@NotNull List<BodyWeight> bodyWeights) {
        Double weight = null;
        LocalDate mostRecentDate = null;
        for (BodyWeight bodyWeight : bodyWeights) {
            if (mostRecentDate == null || bodyWeight.date().isAfter(mostRecentDate)) {
                weight = bodyWeight.value();
                mostRecentDate = bodyWeight.date();
            }
        }

        return weight;
    }
}
