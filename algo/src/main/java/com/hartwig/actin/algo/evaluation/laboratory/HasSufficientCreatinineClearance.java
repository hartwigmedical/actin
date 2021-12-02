package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
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
            return LabValueEvaluation.evaluateVersusMinValue(clearance.value(), clearance.comparator(), minCreatinineClearance);
        }

        // If no clearance value was found, we derive it from creatinine. See also https://www.knmp.nl/rekenmodules/creatinine_html
        LabValue creatinine = interpretation.mostRecentValue(LabMeasurement.CREATININE);

        if (creatinine == null) {
            return Evaluation.UNDETERMINED;
        }

        if (!creatinine.unit().toLowerCase().equals("umol/l")) {
            LOGGER.warn("Suspicious unit detected for creatinine measurement: '{}'. Cannot determine clearance.", creatinine.unit());
            return Evaluation.UNDETERMINED;
        }

        switch (method) {
            case EGFR_MDRD:
                return evaluateMDRD(record, creatinine);
            default:
                return Evaluation.NOT_IMPLEMENTED;
        }

        /*

        Cockcroft Gault
        (140 - leeftijd in jaren) × gewicht / (0.81 × serum creatinine in micromol/l)

        bij vrouwen: uitkomst vermenigvuldigen met 0.85

        (morbide) obesen met BMI 30 kg/m2 of meer: vul als gewicht het lean body weight (LBW) in.
        Dit kunt u berekenen m.b.t. de rekenformule onder het kopje Lengte en gewicht.

        CKD-EPI
        GFR = 141 × min(Scr/κ, 1)^α × max(Scr/κ, 1)^-1.209 × 0.993^leeftijd

        bij vrouwen: × 1.018
        bij negroïde personen: × 1.159

        Scr is serum creatinine in µmol/L
        κ is 61.9 voor vrouwen en 79.6 voor mannen
        α is -0.329 voor vrouwen en -0.411 voor mannen
        min is minimum van Scr/κ of 1
        max is maximum van Scr/κ of 1
        */
    }

    @NotNull
    Evaluation evaluateMDRD(@NotNull PatientRecord record, @NotNull LabValue creatinine) {
        List<Double> mdrdValues = toMDRD(record, creatinine);
        Set<Evaluation> evaluations = Sets.newHashSet();
        for (Double mdrdValue : mdrdValues) {
            evaluations.add(LabValueEvaluation.evaluateVersusMinValue(mdrdValue, creatinine.comparator(), minCreatinineClearance));
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

    @VisibleForTesting
    @NotNull
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

    @Nullable
    private LabValue retrieveForMethod(@NotNull LabInterpretation interpretation) {
        switch (method) {
            case EGFR_CDK_EPI:
                return interpretation.mostRecentValue(LabMeasurement.EGFR_CDK_EPI);
            case EGFR_MDRD:
                return interpretation.mostRecentValue(LabMeasurement.EGFR_MDRD);
            case COCKCROFT_GAULT:
                return interpretation.mostRecentValue(LabMeasurement.CREATININE_CLEARANCE_CG);
            default: {
                LOGGER.warn("Cannot resolve lab value for creatinine clearance method '{}'", method);
                return null;
            }
        }
    }
}
