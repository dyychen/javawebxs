$ErrorActionPreference = 'Stop'

Set-Location -LiteralPath (Join-Path $PSScriptRoot 'frontend')
& (Get-Command npm.cmd).Source run dev
