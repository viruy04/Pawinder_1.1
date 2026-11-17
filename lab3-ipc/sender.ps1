$ErrorActionPreference = "Stop"

$pipeName = "PawinderLab3Pipe"
$pipe = [System.IO.Pipes.NamedPipeClientStream]::new(
    ".",
    $pipeName,
    [System.IO.Pipes.PipeDirection]::Out
)

try {
    Write-Host "Sender PID: $PID"
    Write-Host "Connecting to receiver..."
    $pipe.Connect(10000)

    $messages = @(
        "Первое сообщение",
        "Второе сообщение для проверки",
        "Третье сообщение"
    )

    foreach ($message in $messages) {
        $body = [System.Text.Encoding]::UTF8.GetBytes($message)
        $header = [BitConverter]::GetBytes([int]$body.Length)

        $pipe.Write($header, 0, $header.Length)
        $pipe.Flush()

        $offset = 0
        while ($offset -lt $body.Length) {
            $chunkSize = [Math]::Min(2, $body.Length - $offset)
            $pipe.Write($body, $offset, $chunkSize)
            $pipe.Flush()
            $offset += $chunkSize
        }

        Write-Host "Sent ($($body.Length) bytes): $message"
    }

    Read-Host "Close receiver first, then press Enter here"

    $message = "Сообщение после закрытия приёмника"
    $body = [System.Text.Encoding]::UTF8.GetBytes($message)
    $header = [BitConverter]::GetBytes([int]$body.Length)

    $pipe.Write($header, 0, $header.Length)
    $pipe.Flush()
    $pipe.Write($body, 0, $body.Length)
    $pipe.Flush()

    Write-Host "Unexpectedly sent another message."
}
catch {
    Write-Host "Sender error: $($_.Exception.Message)"
    exit 1
}
finally {
    $pipe.Dispose()
    Write-Host "Sender closed the pipe."
}
