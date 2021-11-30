package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasSufficientEGFR implements EvaluationFunction {

    private static final Logger LOGGER = LogManager.getLogger(HasSufficientEGFR.class);

    @NotNull
    private final EGFRMethod method;
    private final double minEGFR;

    HasSufficientEGFR(@NotNull final EGFRMethod method, final double minEGFR) {
        this.method = method;
        this.minEGFR = minEGFR;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue egfr = retrieveForMethod(interpretation);

        if (egfr != null) {
            return LabValueEvaluation.evaluateVersusMinValue(egfr.value(), egfr.comparator(), minEGFR);
        }

        // If no EGFR value was found, we derive it from creatinine. See also https://www.knmp.nl/rekenmodules/creatinine_html
        LabValue creatinine = interpretation.mostRecentValue(LabMeasurement.CREATININE);

        if (creatinine == null) {
            return Evaluation.UNDETERMINED;
        }

        // TODO Implement
        return Evaluation.NOT_IMPLEMENTED;
        /*
        MDRD
        GFR (mL/min/1.73 m²) = 175 × (Scr/88.4)^-1.154 × (leeftijd)^-0.203

        bij vrouwen: × 0.742
        bij negroïde personen: × 1.212

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

    @Nullable
    private LabValue retrieveForMethod(@NotNull LabInterpretation interpretation) {
        switch (method) {
            case CDK_EPI:
                return interpretation.mostRecentValue(LabMeasurement.EGFR_CDK_EPI);
            case MDRD:
                return interpretation.mostRecentValue(LabMeasurement.EGFR_MDRD);
            default: {
                LOGGER.warn("Cannot resolve lab value for EGFR method '{}'", method);
                return null;
            }
        }
    }
}
