package com.hartwig.actin.clinical.datamodel.treatment

enum class DrugType(override val category: TreatmentCategory, private val display: String? = null) : TreatmentType {
    ABL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ABL inhibitor"),
    ABL_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ABL TKI"),
    ABL1_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ABL1 TKI"),
    ADENOSINE_TARGETING(TreatmentCategory.IMMUNOTHERAPY),
    ALK_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ALK inhibitor"),
    ALK_INHIBITOR_GEN_1(TreatmentCategory.TARGETED_THERAPY, "ALK inhibitor (1st gen)"),
    ALK_INHIBITOR_GEN_2(TreatmentCategory.TARGETED_THERAPY, "ALK inhibitor (2nd gen)"),
    ALK_INHIBITOR_GEN_3(TreatmentCategory.TARGETED_THERAPY, "ALK inhibitor (3rd gen)"),
    ALK_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ALK TKI"),
    ALKYLATING_AGENT(TreatmentCategory.CHEMOTHERAPY),
    ANTHRACYCLINE(TreatmentCategory.CHEMOTHERAPY),
    ANTI_ANDROGEN(TreatmentCategory.HORMONE_THERAPY, "anti-androgen"),
    ANTI_B7H4(TreatmentCategory.IMMUNOTHERAPY, "anti-B7H4"),
    ANTI_CD137(TreatmentCategory.IMMUNOTHERAPY, "anti-CD137"),
    ANTI_CD3(TreatmentCategory.TARGETED_THERAPY, "anti-CD3"),
    ANTI_CD40_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "anti-CD40 antibody"),
    ANTI_CD9_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "anti-CD9 antibody"),
    ANTI_CEACAM5(TreatmentCategory.TARGETED_THERAPY, "anti-CEACAM5"),
    ANTI_CLDN6_CAR_T(TreatmentCategory.IMMUNOTHERAPY, "anti-CLDN6 CAR-T"),
    ANTI_ESTROGEN(TreatmentCategory.HORMONE_THERAPY, "anti-estrogen"),
    ANTI_KLK2(TreatmentCategory.TARGETED_THERAPY, "anti-KLK2"),
    ANTI_LAG_3(TreatmentCategory.IMMUNOTHERAPY, "anti-LAG-3"),
    ANTI_OX40(TreatmentCategory.IMMUNOTHERAPY, "anti-OX40"),
    ANTI_PD_1_LAG_3(TreatmentCategory.IMMUNOTHERAPY, "anti-PD-1/LAG-3"),
    ANTI_PD_1_TIM_3(TreatmentCategory.IMMUNOTHERAPY, "anti-PD-1/TIM-3"),
    ANTI_PD_1(TreatmentCategory.IMMUNOTHERAPY, "anti-PD-1"),
    ANTI_PD_L1(TreatmentCategory.IMMUNOTHERAPY, "anti-PD-L1"),
    ANTI_PD_L2(TreatmentCategory.IMMUNOTHERAPY, "anti-PD-L2"),
    ANTI_TIGIT(TreatmentCategory.IMMUNOTHERAPY, "anti-TIGIT"),
    ANTI_TIM_3(TreatmentCategory.IMMUNOTHERAPY, "anti-TIM-3"),
    ANTI_TISSUE_FACTOR(TreatmentCategory.TARGETED_THERAPY, "anti tissue-factor (TF)"),
    ANTIBIOTIC(TreatmentCategory.SUPPORTIVE_TREATMENT, "antibiotic"),
    ANTIBODY_DRUG_CONJUGATE_IMMUNOTHERAPY(TreatmentCategory.IMMUNOTHERAPY, "antibody-drug conjugate"),
    ANTIBODY_DRUG_CONJUGATE_TARGETED_THERAPY(TreatmentCategory.TARGETED_THERAPY, "antibody-drug conjugate"),
    ANTIMETABOLITE(TreatmentCategory.CHEMOTHERAPY),
    ANTIMICROTUBULE_AGENT(TreatmentCategory.CHEMOTHERAPY),
    AROMATASE_INHIBITOR(TreatmentCategory.HORMONE_THERAPY),
    AUTOLOGOUS_CELL_IMMUNOTHERAPY(TreatmentCategory.IMMUNOTHERAPY),
    AXL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "AXL inhibitor"),
    AXL_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "AXL TKI"),
    BCR_ABL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "BCR-ABL inhibitor"),
    BCR_ABL_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "BCR-ABL TKI"),
    BISPHOSPHONATE(TreatmentCategory.SUPPORTIVE_TREATMENT),
    BRAF_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "BRAF inhibitor"),
    BRAF_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "BRAF TKI"),
    CALCIUM_REGULATORY_MEDICATION(TreatmentCategory.SUPPORTIVE_TREATMENT),
    CD73_ANTIBODY(TreatmentCategory.IMMUNOTHERAPY, "CD73 antibody"),
    CDK_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CDK inhibitor"),
    CDK2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CDK2 inhibitor"),
    CDK4_6_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CDK4/6 inhibitor"),
    CDK4_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CDK4 inhibitor"),
    CDK6_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CDK6 inhibitor"),
    CORTICOSTEROID(TreatmentCategory.SUPPORTIVE_TREATMENT),
    CRAF_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CRAF inhibitor"),
    CRAF_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CRAF TKI"),
    CSF1R_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CSF1R inhibitor"),
    CSF1R_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CSF1R TKI"),
    CTLA4_INHIBITOR(TreatmentCategory.IMMUNOTHERAPY, "CTLA4 inhibitor"),
    CYTOKINE(TreatmentCategory.IMMUNOTHERAPY),
    DDR1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "DDR1 inhibitor"),
    DDR1_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "DDR1 TKI"),
    DDR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "DDR2 inhibitor"),
    DDR2_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "DDR2 TKI"),
    EGFR_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "EGFR antibody"),
    EGFR_C797X_TKI(TreatmentCategory.TARGETED_THERAPY, "EGFR C797X targeting TKI"),
    EGFR_EXON_20_INS_BISPECIFIC_ANTIBODIES(TreatmentCategory.TARGETED_THERAPY, "EGFR ex20ins bispecific antibody"),
    EGFR_EXON_20_INS_TARGETED_THERAPY(TreatmentCategory.TARGETED_THERAPY, "EGFR ex20ins targeting therapy"),
    EGFR_EXON_20_INS_TKI(TreatmentCategory.TARGETED_THERAPY, "EGFR ex20ins targeting TKI"),
    EGFR_INHIBITOR_GEN_1(TreatmentCategory.TARGETED_THERAPY, "EGFR inhibitor (1st gen)"),
    EGFR_INHIBITOR_GEN_2(TreatmentCategory.TARGETED_THERAPY, "EGFR inhibitor (2nd gen)"),
    EGFR_INHIBITOR_GEN_3(TreatmentCategory.TARGETED_THERAPY, "EGFR inhibitor (3rd gen)"),
    EGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "EGFR inhibitor"),
    EGFR_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "EGFR TKI"),
    ERBB2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ERBB2 inhibitor"),
    FGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR inhibitor"),
    FGFR_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR TKI"),
    FGFR1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR1 inhibitor"),
    FGFR1_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR1 TKI"),
    FGFR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR2 inhibitor"),
    FGFR2_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR2 TKI"),
    FGFR3_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR3 inhibitor"),
    FGFR3_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR3 TKI"),
    FLT3_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FLT3 inhibitor"),
    FLT3_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FLT3 TKI"),
    FLT3L_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FLT3L inhibitor"),
    FLUOROPYRIMIDINE(TreatmentCategory.CHEMOTHERAPY),
    FOLR1_TARGETING_THERAPY(TreatmentCategory.TARGETED_THERAPY, "FOLR1 targeting therapy"),
    GAMMA_SECRETASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    GONADORELIN_AGONIST(TreatmentCategory.HORMONE_THERAPY),
    HEDGEHOG_PATHWAY_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    HER_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "HER TKI"),
    HER2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "HER2 antibody"),
    HER2_CAR_T(TreatmentCategory.IMMUNOTHERAPY, "HER2 CAR-T"),
    HER2_EXON_20_INS_TKI(TreatmentCategory.TARGETED_THERAPY, "HER2 ex20ins targeting TKI"),
    HER2_IMMUNOTHERAPY(TreatmentCategory.IMMUNOTHERAPY, "HER2 antibody"),
    HER2_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "HER2 TKI"),
    HER3_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "HER3 antibody"),
    HIF_2A_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "HIF-2α inhibitor"),
    HORMONE_ANTINEOPLASTIC(TreatmentCategory.HORMONE_THERAPY),
    HPV_VACCINE(TreatmentCategory.IMMUNOTHERAPY, "HPV vaccine"),
    HPV16_VACCINE(TreatmentCategory.IMMUNOTHERAPY, "HPV-16 vaccine"),
    IDO1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "IDO1 inhibitor"),
    IL_15_CYTOKINE(TreatmentCategory.IMMUNOTHERAPY, "IL-15 cytokine"),
    IL_2_CYTOKINE(TreatmentCategory.IMMUNOTHERAPY, "IL-2 cytokine"),
    IMMUNE_CHECKPOINT_INHIBITOR(TreatmentCategory.IMMUNOTHERAPY),
    KIT_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "KIT inhibitor"),
    KIT_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "KIT TKI"),
    KRAS_G12C_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "KRAS G12C inhibitor"),
    MEK_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "MEK inhibitor"),
    MEK1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "MEK1 inhibitor"),
    MEK2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "MEK2 inhibitor"),
    MET_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "MET antibody"),
    MET_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "MET inhibitor"),
    MET_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "MET TKI"),
    MONOCLONAL_ANTIBODY_IMMUNOTHERAPY(TreatmentCategory.IMMUNOTHERAPY, "monoclonal antibody - immunotherapies"),
    MONOCLONAL_ANTIBODY_MMAE_CONJUGATE(TreatmentCategory.TARGETED_THERAPY, "monoclonal antibody - MMAE conjugate"),
    MONOCLONAL_ANTIBODY_SUPPORTIVE_TREATMENT(TreatmentCategory.SUPPORTIVE_TREATMENT, "monoclonal antibody - supportive treatments"),
    MONOCLONAL_ANTIBODY_TARGETED_THERAPY(TreatmentCategory.TARGETED_THERAPY, "monoclonal antibody - targeted therapies"),
    MTORC1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "MTORC1 inhibitor"),
    MUC16_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "MUC16 inhibitor"),
    NK_CELL_BASED_THERAPY(TreatmentCategory.IMMUNOTHERAPY, "NK cell-based therapy"),
    NONSTEROIDAL_ANTI_ANDROGEN(TreatmentCategory.HORMONE_THERAPY),
    NOTCH_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    NOVEL_HORMONE_THERAPY_FOR_PROSTATE(TreatmentCategory.HORMONE_THERAPY),
    PARP_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PARP inhibitor"),
    PD_1_PD_L1_ANTIBODY(TreatmentCategory.IMMUNOTHERAPY, "PD-1/PD-L1 antibody"),
    PDGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PDGFR inhibitor"),
    PDGFR_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PDGFR TKI"),
    PDGFRA_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PDGFRA inhibitor"),
    PDGFRA_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PDGFRA TKI"),
    PDGFRB_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PDGFRB inhibitor"),
    PDGFRB_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PDGFRB TKI"),
    PIK3CA_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PIK3CA inhibitor"),
    PLATINUM_COMPOUND(TreatmentCategory.CHEMOTHERAPY),
    PROGESTIN(TreatmentCategory.HORMONE_THERAPY),
    PYRIMIDINE_ANTAGONIST(TreatmentCategory.CHEMOTHERAPY),
    RAF_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "RAF inhibitor"),
    RAF_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "RAF TKI"),
    RANKL_ANTIBODY(TreatmentCategory.SUPPORTIVE_TREATMENT, "RANKL antibody"),
    RET_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "RET inhibitor"),
    RET_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "RET TKI"),
    RON_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "RON inhibitor"),
    RON_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "RON TKI"),
    ROS1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ROS1 inhibitor"),
    ROS1_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ROS1 TKI"),
    SHP2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "SHP2 inhibitor"),
    SMO_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "SMO inhibitor"),
    SRC_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "SRC inhibitor"),
    STEROID(TreatmentCategory.SUPPORTIVE_TREATMENT),
    TAXANE(TreatmentCategory.CHEMOTHERAPY),
    THYMIDINE_PHOSPHORYLASE_INHIBITOR(TreatmentCategory.CHEMOTHERAPY),
    TOPO1_INHIBITOR(TreatmentCategory.CHEMOTHERAPY, "TOPO1 inhibitor"),
    TOPO2_INHIBITOR(TreatmentCategory.CHEMOTHERAPY, "TOPO2 inhibitor"),
    TRK_RECEPTOR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "TRK receptor inhibitor"),
    TRK_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "TRK TKI"),
    TROP2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "TROP2 antibody"),
    TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    VACCINE(TreatmentCategory.IMMUNOTHERAPY),
    VEGF_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "VEGF antibody"),
    VEGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "VEGFR inhibitor"),
    VEGFR_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "VEGFR TKI"),
    VEGFR2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "VEGFR2 antibody"),
    VEGFR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "VEGFR2 inhibitor"),
    VEGFR2_TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "VEGFR2 TKI");

    override fun display(): String {
        return display ?: toString().replace("_", " ").lowercase()
    }
}