# Example patients and regression tests

## Example patients

Example treatment matches and reports can be made using the `LocalExampleTreatmentMatchApplication` and `LocalExampleReportApplication`
respectively by assigning the `EXAMPLE_TO_RUN` to the ID of the example patient.

| ID             | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| LUNG-01        | An example of a representative lung cancer patient, intended for externally shared example reports.<br/>Primary: St. IV NSCLC (adenocarcinoma), liver and lung lesions, 1L Osimertinib.<br/>Molecular: EGFR L858R + C797S, PD-L1 >50%.                                                                                                                                                                                                                                                                                                                                   |
| TEST-NSCLC-WGS | WGS test case created for validation/evaluation of trial matching on per in-/exclusion criterion level.<br/>Primary: St. IV NSCLC (adenocarcinoma), brain, lung, and lymph node lesions, 1L Osimertinib.<br/>Prior skin squamous cell carcinoma (inactive).<br/>Molecular: EGFR L858R + C797S, PD-L1 >50%, TMB High, No HLA A*02:01.<br/>History: WHO 0, rheumatoid ahritis, active pneumonia, hypertension, recent cholecystecomy.<br/>Lab: recent erythrocyte transfusion, decreased thrombocytes.<br/>Medication: St. John's wort (CYP-interaction).                  |
| TEST-NSCLC-NGS | NGS test case created for validation/evaluation of trial matching on per in-/exclusion criterion level.<br/>Primary: St. III NSCLC (squamous cell carcinoma), liver node lesions, 1L platinum doublet with pemetrexed, 1L docetaxel (stop reason: tox).<br/>Molecular: No targetable drivers, PIK3CA E545K, TP53 loss, KEAP1 loss, TMB low, PD-L1 <1%.<br/>History: WHO 1, Cardiac history (myocardial infarction, LBBB, LVEF 0.4), bipolar disorder.<br/>Lab: ASAT + ALAT 4x ULN, but within boundries for liver metastases.<br/>Medication: Lithium (QT-prolongation). |
| TEST-SCLC-NGS  | NGS test case created for validation/evaluation of trial matching on per in-/exclusion criterion level.<br/>Primary: St. IV SCLC, bone lesions, 1L Cisplatin+Etoposide, PR.<br/>Molecular: Small panel, several genes missing for complete evaluation. Hotspot: TP53 R273H and SMARCA4 T910M.<br/>History: WHO 2, malignant pleural effusion, cerebrovascular accident 5 years prior.<br/>Lab: Alkaline phosphatase 4x ULN, but within boundries for bone metastases.<br/>Medication: Simvastatin and Clopidogrel (both CYP-interaction).                                |

## Report regression test

The report regression test calls the local reporter application with all examples.
It asserts against both whether the text of the report matches and whether they are visually identical.

The visual test leverages the iText compare tool. It turns everything to images then gives diffs per page as images with the differences
highlighted. This tool uses two external tools: ghostscript and image magick. So we add a docker container with these tools to use as a
build agent. You must set this up locally using [brew](https://brew.sh/)

```commandline
brew install gs
brew install imagemagick
```

When making a reporting change, the workflow is as follows:

- These tests will be expected to fail after the report is changed.
- Developer should check that the differences match up with the change they intended to make and nothing else. The text comparison will be
  seen directly in the test result. The image diffs can be found in `system/test-classes/EXAMPLE.actin.pdf.diff.*`
    - If something unexpected changes, fix it.
    - When only the intended change, we simply copy the new report from `system/test-classes` into the `example_reports` directory and the
      test passes.

