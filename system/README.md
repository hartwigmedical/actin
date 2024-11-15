# Example patients and regression tests

## Example patients

Example treatment matches and reports can be made using the `LocalExampleTreatmentMatchApplication` and `LocalExampleReportApplication`
respectively by assigning the `EXAMPLE_TO_RUN` to the ID of the example patient.

| ID      | Description                                        |
|---------|----------------------------------------------------|
| LUNG-01 | An example of a representative lung cancer patient |

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
  seen
  directly in the test result. The image diffs can be found in `system/test-classes/EXAMPLE.actin.pdf.diff.*`
    - If something unexpected changes, fix it.
    - When only the intended change, we simply copy the new report from `system/test-classes` into the `example_reports` directory and the
      test passes.

