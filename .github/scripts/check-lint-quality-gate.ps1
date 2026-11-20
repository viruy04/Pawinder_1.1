
$reportPath = "app/build/reports/lint-results-debug.sarif"

if (-not (Test-Path $reportPath)) {
    Write-Error "Lint report not found: $reportPath"
    exit 2
}

$sarif = Get-Content $reportPath -Raw | ConvertFrom-Json

$violations = @(
    foreach ($run in $sarif.runs) {
        foreach ($result in $run.results) {
            if ($result.ruleId -eq "DefaultLocale") {
                $result
            }
        }
    }
)

if ($violations.Count -gt 0) {
    Write-Host "QUALITY GATE FAILED: DefaultLocale findings detected."
    foreach ($finding in $violations) {
        Write-Host "- $($finding.message.text)"
    }
    exit 1
}

Write-Host "QUALITY GATE PASSED: no DefaultLocale findings."
exit 0
