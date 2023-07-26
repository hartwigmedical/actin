package com.hartwig.actin.clinical.datamodel.treatment;

import org.jetbrains.annotations.NotNull;

public enum DrugType implements TreatmentType {
    ABL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    ALK_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    ALKYLATING_AGENT(TreatmentCategory.CHEMOTHERAPY),
    ANTHRACYCLINE(TreatmentCategory.CHEMOTHERAPY),
    ANTIBODY_DRUG_CONJUGATE_IMMUNOTHERAPY(TreatmentCategory.IMMUNOTHERAPY),
    ANTIBODY_DRUG_CONJUGATE_TARGETED_THERAPY(TreatmentCategory.TARGETED_THERAPY),
    ANTIMETABOLITE(TreatmentCategory.CHEMOTHERAPY),
    ANTIMICROTUBULE_AGENT(TreatmentCategory.CHEMOTHERAPY),
    ANTI_ANDROGEN(TreatmentCategory.HORMONE_THERAPY),
    ANTI_CD3(TreatmentCategory.TARGETED_THERAPY),
    ANTI_CD9(TreatmentCategory.TARGETED_THERAPY),
    ANTI_CD40(TreatmentCategory.TARGETED_THERAPY),
    ANTI_CEACAM5(TreatmentCategory.TARGETED_THERAPY),
    ANTI_CLDN6_CAR_T(TreatmentCategory.CAR_T),
    ANTI_ESTROGEN(TreatmentCategory.HORMONE_THERAPY),
    ANTI_KLK2(TreatmentCategory.TARGETED_THERAPY),
    ANTI_PD_1(TreatmentCategory.IMMUNOTHERAPY),
    ANTI_PD_L1(TreatmentCategory.IMMUNOTHERAPY),
    ANTI_PD_L2(TreatmentCategory.IMMUNOTHERAPY),
    ANTI_TISSUE_FACTOR(TreatmentCategory.TARGETED_THERAPY),
    AROMATASE_INHIBITOR(TreatmentCategory.HORMONE_THERAPY),
    AXL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    BCR_ABL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    BRAF_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    CALCIUM_REGULATORY_MEDICATION(TreatmentCategory.SUPPORTIVE_TREATMENT),
    CDK4_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    CDK6_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    CSF1R_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    DDR1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    DDR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    EGFR_ANTIBODY(TreatmentCategory.TARGETED_THERAPY),
    FGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    FGFR1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    FGFR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    FGFR3_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    FLT3_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    FLUOROPYRIMIDINE(TreatmentCategory.CHEMOTHERAPY),
    GONADORELIN_AGONISTS(TreatmentCategory.HORMONE_THERAPY),
    HER2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY),
    HORMONE_ANTINEOPLASTICS(TreatmentCategory.HORMONE_THERAPY),
    IDO1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    KIT_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    MET_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    MONOCLONAL_ANTIBODY_IMMUNOTHERAPY(TreatmentCategory.IMMUNOTHERAPY),
    MONOCLONAL_ANTIBODY_TARGETED_THERAPY(TreatmentCategory.TARGETED_THERAPY),
    MTORC1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    NONSTEROIDAL_ANTI_ANDROGEN(TreatmentCategory.HORMONE_THERAPY),
    NOTCH_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    NOVEL_HORMONE_THERAPY_FOR_PROSTATE(TreatmentCategory.HORMONE_THERAPY),
    PARP_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    PDGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    PDGFRA_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    PDGFRB_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    PLATINUM_COMPOUND(TreatmentCategory.CHEMOTHERAPY),
    PROGESTIN(TreatmentCategory.HORMONE_THERAPY),
    PYRIMIDINE_ANTAGONIST(TreatmentCategory.CHEMOTHERAPY),
    RAF_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    RET_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    ROS1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    SMO_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    TAXANE(TreatmentCategory.CHEMOTHERAPY),
    THYMIDINE_PHOSPHORYLASE_INHIBITOR(TreatmentCategory.CHEMOTHERAPY),
    TOPO1_INHIBITOR(TreatmentCategory.CHEMOTHERAPY),
    TOPO2_INHIBITOR(TreatmentCategory.CHEMOTHERAPY),
    TRK_RECEPTOR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    TROP2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY),
    TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    VACCINE(TreatmentCategory.PROPHYLACTIC_TREATMENT),
    VEGF_ANTIBODY(TreatmentCategory.TARGETED_THERAPY),
    VEGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    VEGFR2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY),
    VEGFR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY);

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