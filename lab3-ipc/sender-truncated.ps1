$pipe = [System.IO.Pipes.NamedPipeClientStream]::new(
    ".",
    "PawinderLab3Pipe",
    [System.IO.Pipes.PipeDirection]::Out
)

try {
    $pipe.Connect(10000)

    # Объявляем длину тела 10 байт.
    $header = [BitConverter]::GetBytes([int]10)
    $pipe.Write($header, 0, $header.Length)

    # Передаём только 3 байта вместо 10.
    $body = [byte[]](65, 66, 67)
    $pipe.Write($body, 0, $body.Length)
    $pipe.Flush()

    Write-Host "Sent incomplete frame: 3 of 10 bytes."
}
finally {
    $pipe.Dispose()
    Write-Host "Test sender closed the pipe."
}
