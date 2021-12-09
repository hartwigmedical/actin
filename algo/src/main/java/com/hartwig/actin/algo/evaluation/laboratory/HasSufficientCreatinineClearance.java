package com.hartwig.actin.algo.evaluation.laboratory;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasSufficientCreatinineClearance implements EvaluationFunction {

    private static final Logger LOGGER = LogManager.getLogger(HasSufficientCreatinineClearance.class);

    private static final double MIN_EGFR_CKD_EPI_FALL_BACK_COCKCROFT_GAULT = 65D;

    private final int referenceYear;
    @NotNull
    private final CreatinineClearanceMethod method;
    private final double minCreatinineClearance;

    HasSufficientCreatinineClearance(final int referenceYear, @NotNull final CreatinineClearanceMethod method,
            final double minCreatinineClearance) {
        this.referenceYear = referenceYear;
        this.method = method;
        this.minCreatinineClearance = minCreatinineClearance;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue clearance = retrieveForMethod(interpretation);

        if (clearance != null) {
            return LaboratoryUtil.evaluateVersusMinValue(clearance.value(), clearance.comparator(), minCreatinineClearance);
        }

        // If no clearance value was found, we derive it from creatinine. See also https://www.knmp.nl/rekenmodules/creatinine_html
        LabValue creatinine = interpretation.mostRecentValue(LabMeasurement.CREATININE);

        if (!LaboratoryUtil.existsWithExpectedUnit(creatinine, LabMeasurement.CREATININE.expectedUnit())) {
            return Evaluation.UNDETERMINED;
        }

        switch (method) {
            case EGFR_MDRD:
                return evaluateMDRD(record, creatinine);
            case EGFR_CKD_EPI:
                return evaluateCKDEPI(record, creatinine);
            case COCKCROFT_GAULT:
                return evaluateCockcroftGault(record, creatinine, interpretation.mostRecentValue(LabMeasurement.EGFR_CKD_EPI));
            default: {
                LOGGER.warn("No creatinine clearance function implemented for '{}'", method);
                return Evaluation.NOT_IMPLEMENTED;
            }
        }
    }

    @Nullable
    private LabValue retrieveForMethod(@NotNull LabInterpretation interpretation) {
        switch (method) {
            case EGFR_MDRD:
                return interpretation.mostRecentValue(LabMeasurement.EGFR_MDRD);
            case EGFR_CKD_EPI:
                return interpretation.mostRecentValue(LabMeasurement.EGFR_CKD_EPI);
            case COCKCROFT_GAULT:
                return interpretation.mostRecentValue(LabMeasurement.CREATININE_CLEARANCE_CG);
            default: {
                LOGGER.warn("Cannot resolve lab value for creatinine clearance method '{}'", method);
                return null;
            }
        }
    }

    @NotNull
    private Evaluation evaluateMDRD(@NotNull PatientRecord record, @NotNull LabValue creatinine) {
        return evaluateValues(toMDRD(record, creatinine), creatinine.comparator());
    }

    @NotNull
    private Evaluation evaluateValues(@NotNull List<Double> values, @NotNull String comparator) {
        Set<Evaluation> evaluations = Sets.newHashSet();
        for (Double value : values) {
            evaluations.add(LaboratoryUtil.evaluateVersusMinValue(value, comparator, minCreatinineClearance));
        }

        if (evaluations.contains(Evaluation.FAIL)) {
            return evaluations.contains(Evaluation.PASS) ? Evaluation.UNDETERMINED : Evaluation.FAIL;
        } else if (evaluations.contains(Evaluation.UNDETERMINED)) {
            return Evaluation.UNDETERMINED;
        } else {
            // Every value should be pass.
            return Evaluation.PASS;
        }
    }

    @NotNull
    @VisibleForTesting
    List<Double> toMDRD(@NotNull PatientRecord record, @NotNull LabValue creatinine) {
        List<Double> mdrdValues = Lists.newArrayList();

        int age = referenceYear - record.clinical().patient().birthYear();

        double base = 175 * Math.pow(creatinine.value() / 88.4, -1.154) * Math.pow(age, -0.203);

        double adjusted = base;
        if (record.clinical().patient().gender() == Gender.FEMALE) {
            adjusted = base * 0.742;
        }

        mdrdValues.add(adjusted);
        mdrdValues.add(adjusted * 1.212);

        return mdrdValues;
    }

    @NotNull
    private Evaluation evaluateCKDEPI(@NotNull PatientRecord record, @NotNull LabValue creatinine) {
        return evaluateValues(toCKDEPI(record, creatinine), creatinine.comparator());
    }

    @NotNull
    @VisibleForTesting
    List<Double> toCKDEPI(@NotNull PatientRecord record, @NotNull LabValue creatinine) {
        List<Double> ckdepiValues = Lists.newArrayList();

        int age = referenceYear - record.clinical().patient().birthYear();

        boolean isFemale = record.clinical().patient().gender() == Gender.FEMALE;
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
    private Evaluation evaluateCockcroftGault(@NotNull PatientRecord record, @NotNull LabValue creatinine, @Nullable LabValue egfrCKDEPI) {
        Double weight = determineWeight(record.clinical().bodyWeights());
        if (weight == null) {
            if (egfrCKDEPI == null) {
                return Evaluation.UNDETERMINED;
            }

            return egfrCKDEPI.value() > MIN_EGFR_CKD_EPI_FALL_BACK_COCKCROFT_GAULT ? Evaluation.PASS_BUT_WARN : Evaluation.UNDETERMINED;
        }

        int age = referenceYear - record.clinical().patient().birthYear();

        double base = (140 - age) * weight / (0.81 * creatinine.value());

        double adjusted = base;
        if (record.clinical().patient().gender() == Gender.FEMALE) {
            adjusted = base * 0.85;
        }

        return LaboratoryUtil.evaluateVersusMinValue(adjusted, creatinine.comparator(), minCreatinineClearance);
    }

    @Nullable
    private static Double determineWeight(@NotNull List<BodyWeight> bodyWeights) {
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
