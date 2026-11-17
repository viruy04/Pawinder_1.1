$ErrorActionPreference = "Stop"

$pipeName = "PawinderLab3Pipe"
$maxFrameSize = 1024

$pipe = [System.IO.Pipes.NamedPipeServerStream]::new(
    $pipeName,
    [System.IO.Pipes.PipeDirection]::In,
    1,
    [System.IO.Pipes.PipeTransmissionMode]::Byte,
    [System.IO.Pipes.PipeOptions]::None
)

try {
    Write-Host "Receiver PID: $PID"
    Write-Host "Waiting for sender..."

    $pipe.WaitForConnection()
    Write-Host "Sender connected."

    while ($true) {
        $header = New-Object byte[] 4
        $headerRead = 0

        while ($headerRead -lt 4) {
            $count = $pipe.Read($header, $headerRead, 4 - $headerRead)

            if ($count -eq 0) {
                if ($headerRead -eq 0) {
                    Write-Host "Normal EOF: sender closed the pipe."
                    break
                }

                throw "Truncated frame header."
            }

            $headerRead += $count
        }

        if ($headerRead -eq 0) {
            break
        }

        if ($headerRead -lt 4) {
            throw "Truncated frame header."
        }

        $length = [BitConverter]::ToInt32($header, 0)

        if ($length -lt 0 -or $length -gt $maxFrameSize) {
            throw "Invalid frame length: $length"
        }

        $body = New-Object byte[] $length
        $bodyRead = 0

        while ($bodyRead -lt $length) {
            $chunkSize = [Math]::Min(3, $length - $bodyRead)
            $count = $pipe.Read($body, $bodyRead, $chunkSize)

            if ($count -eq 0) {
                throw "Truncated frame body."
            }

            $bodyRead += $count
        }

        $message = [System.Text.Encoding]::UTF8.GetString($body)
        Write-Host "Received ($length bytes): $message"
    }

    Read-Host "Receiver is still running. Press Enter to close"
}
catch {
    Write-Host "Receiver error: $($_.Exception.Message)"
    exit 1
}
finally {
    $pipe.Dispose()
    Write-Host "Pipe closed."
}
