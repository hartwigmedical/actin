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

public class HasSufficientCreatinineClearance implements EvaluationFunction {

    private static final Logger LOGGER = LogManager.getLogger(HasSufficientCreatinineClearance.class);

    @NotNull
    private final CreatinineClearanceMethod method;
    private final double minCreatinineClearance;

    HasSufficientCreatinineClearance(@NotNull final CreatinineClearanceMethod method, final double minCreatinineClearance) {
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
