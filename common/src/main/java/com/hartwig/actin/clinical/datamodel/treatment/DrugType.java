package com.hartwig.actin.clinical.datamodel.treatment;

import org.jetbrains.annotations.NotNull;

public enum DrugType implements TreatmentType {
    ABL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    ALK_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    ALKYLATING_AGENT(TreatmentCategory.CHEMOTHERAPY),
    ANTHRACYCLINE(TreatmentCategory.CHEMOTHERAPY),
    ANTIMETABOLITE(TreatmentCategory.CHEMOTHERAPY),
    ANTIMICROTUBULE_AGENT(TreatmentCategory.CHEMOTHERAPY),
    ANTI_ANDROGEN(TreatmentCategory.HORMONE_THERAPY),
    ANTI_ESTROGEN(TreatmentCategory.HORMONE_THERAPY),
    ANTI_PD_1(TreatmentCategory.IMMUNOTHERAPY),
    ANTI_PD_L1(TreatmentCategory.IMMUNOTHERAPY),
    ANTI_TISSUE_FACTOR(TreatmentCategory.TARGETED_THERAPY),
    AROMATASE_INHIBITOR(TreatmentCategory.HORMONE_THERAPY),
    AXL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    BCR_ABL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    BRAF_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    CALCIUM_REGULATORY_MEDICATION(TreatmentCategory.SUPPORTIVE_TREATMENT),
    //    CDK4_INHIBITOR,
    //    CDK6_INHIBITOR,
    CSF1R_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    DDR1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    DDR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    EGFR_ANTIBODY(TreatmentCategory.TARGETED_THERAPY),
    FGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    FGFR1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    FGFR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    FGFR3_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    FLT3_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    //    GONADORELIN_AGONISTS,
    HER2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY),
    HORMONE_ANTINEOPLASTICS(TreatmentCategory.HORMONE_THERAPY),
    KIT_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    MET_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    //    MONOCLONAL_ANTIBODY,
    MTORC1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    PARP_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    PDGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    PDGFRA_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    PDGFRB_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    PLATINUM_COMPOUND(TreatmentCategory.CHEMOTHERAPY),
    PROGESTIN(TreatmentCategory.HORMONE_THERAPY),
    //    PYRIMIDINE_ANTAGONIST,
    RAF_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    RET_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    ROS1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    SMO_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    THYMIDINE_PHOSPHORYLASE_INHIBITOR(TreatmentCategory.CHEMOTHERAPY),
    TOPO1_INHIBITOR(TreatmentCategory.CHEMOTHERAPY),
    TOPO2_INHIBITOR(TreatmentCategory.CHEMOTHERAPY),
    TRK_RECEPTOR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    TROP2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY),
    VACCINE(TreatmentCategory.PROPHYLACTIC_TREATMENT),
    VEGF_ANTIBODY(TreatmentCategory.TARGETED_THERAPY),
    VEGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    VEGFR2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY),
    VEGFR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY);
    //    VACCINE,

    @NotNull
    private final TreatmentCategory category;

    DrugType(@NotNull TreatmentCategory category) {
        this.category = category;
    }

    @NotNull
    @Override
    public TreatmentCategory category() {
        return category;
    }

    @NotNull
    @Override
    public String display() {
        return toString().replace("_", " ");
    }
}