param(
  [string]$InputDir = (Join-Path $PSScriptRoot '..\\figma-assets\\editable-svg'),
  [string]$OutputPath = (Join-Path $PSScriptRoot '..\\figma-assets\\editable-svg\\Kavvoro-Android-UI-All-Screens.svg')
)

$files = @(Get-ChildItem -LiteralPath $InputDir -Filter '*-editable.svg' | Sort-Object Name)
if ($files.Count -eq 0) { throw "No editable SVG screens found in $InputDir" }

$columns = 3
$cellWidth = 1150
$cellHeight = 1300
$scale = 0.45
$rows = [math]::Ceiling($files.Count / $columns)
$canvasWidth = $columns * $cellWidth
$canvasHeight = $rows * $cellHeight

$parts = [System.Collections.Generic.List[string]]::new()
$parts.Add("<svg xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' width='$canvasWidth' height='$canvasHeight' viewBox='0 0 $canvasWidth $canvasHeight'>")
$parts.Add("<rect width='$canvasWidth' height='$canvasHeight' fill='#10101B'/>")

for ($i = 0; $i -lt $files.Count; $i++) {
  $file = $files[$i]
  $raw = [System.IO.File]::ReadAllText($file.FullName)
  $start = $raw.IndexOf('>') + 1
  $end = $raw.LastIndexOf('</svg>')
  if ($start -lt 1 -or $end -le $start) { continue }
  $inner = $raw.Substring($start, $end - $start)
  $x = ($i % $columns) * $cellWidth + 36
  $y = [math]::Floor($i / $columns) * $cellHeight + 30
  $id = [System.Xml.XmlConvert]::EncodeName($file.BaseName)
  $parts.Add("<g id='$id' transform='translate($x $y) scale($scale)'>")
  $parts.Add("<rect width='1080' height='2400' rx='32' fill='#222238' stroke='#8E8EA8' stroke-width='8'/>")
  $parts.Add($inner)
  $parts.Add('</g>')
}

$parts.Add('</svg>')
$output = $parts -join "`n"
[System.IO.File]::WriteAllText((Resolve-Path (Split-Path -Parent $OutputPath)).Path + '\\' + (Split-Path -Leaf $OutputPath), $output, [System.Text.UTF8Encoding]::new($false))
Write-Output "Generated master SVG with $($files.Count) screens at $OutputPath"
