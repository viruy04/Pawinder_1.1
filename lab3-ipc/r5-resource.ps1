param(
    [ValidateSet("success", "acquisition-failure", "work-failure", "early-exit")]
    [string]$Scenario = "success"
)

$outputPath = Join-Path $PSScriptRoot "r5-result.txt"
$tempPath = Join-Path $PSScriptRoot "r5-result.txt.tmp"
$stream = $null

Remove-Item $outputPath, $tempPath -Force -ErrorAction SilentlyContinue

try {
    if ($Scenario -eq "acquisition-failure") {
        $tempPath = Join-Path $PSScriptRoot "missing-folder\result.tmp"
    }

    $stream = [System.IO.File]::Open(
        $tempPath,
        [System.IO.FileMode]::Create,
        [System.IO.FileAccess]::Write,
        [System.IO.FileShare]::None
    )

    if ($Scenario -eq "work-failure") {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes("Partial data")
        $stream.Write($bytes, 0, $bytes.Length)
        throw "Simulated processing error."
    }

    if ($Scenario -eq "early-exit") {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes("Partial data")
        $stream.Write($bytes, 0, $bytes.Length)
        Write-Host "Operation stopped early."
        return
    }

    $bytes = [System.Text.Encoding]::UTF8.GetBytes("Operation completed successfully.")
    $stream.Write($bytes, 0, $bytes.Length)
    $stream.Flush()

    $stream.Dispose()
    $stream = $null

    Move-Item -Path $tempPath -Destination $outputPath -Force
    Write-Host "SUCCESS: result saved."
}
catch {
    Write-Host "ERROR: $($_.Exception.Message)"
    exit 1
}
finally {
    if ($null -ne $stream) {
        $stream.Dispose()
    }

    if (Test-Path $tempPath) {
        Remove-Item $tempPath -Force
    }

    Write-Host "Resource released; temporary file cleaned up."
}
