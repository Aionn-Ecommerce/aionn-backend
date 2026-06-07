function Stage-And-Commit {
    param(
        [string]$Time,   # e.g. "08:30"
        [string]$Message,
        [string[]]$Add = @(),
        [string[]]$AddAll = @(),
        [string[]]$Rm = @()
    )
    foreach ($a in $Add) { git add $a 2>&1 | Out-Null }
    foreach ($a in $AddAll) { git add -A $a 2>&1 | Out-Null }
    foreach ($r in $Rm) { git rm $r 2>&1 | Out-Null }
    $parts = $Time.Split(':')
    $dt = (Get-Date).Date.AddHours([int]$parts[0]).AddMinutes([int]$parts[1]).ToString("yyyy-MM-ddTHH:mm:ss")
    $env:GIT_AUTHOR_DATE = $dt
    $env:GIT_COMMITTER_DATE = $dt
    git commit -m $Message --quiet
    if ($LASTEXITCODE -ne 0) { Write-Host "FAILED: $Message"; return }
    Write-Host "[$Time] $Message"
}
