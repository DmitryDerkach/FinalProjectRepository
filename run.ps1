# 2026: one-command run of the pharmacy (Windows PowerShell). Needs JDK 11+ and Docker.
#   .\run.ps1                  the same as .\run.ps1 web
#   .\run.ps1 web              build the application, start PostgreSQL + Tomcat 9 in Docker with a fresh database,
#                              walk through the shop with curl (filters, login, roles, cart, admin, language),
#                              show the SQL the application sent, and leave everything running:
#                              open http://localhost:8081/ in a browser
#   .\run.ps1 runner [step]    the console Runner (DAO without a server): all steps, or one of
#                              medicines, users, saveUser, deleteMedicine, findMedicine
#   .\run.ps1 test             JUnit tests: DAO on the database, filters, servlets and JSP on an embedded Tomcat
#   .\run.ps1 reset            recreate the tables and the data (01_schema.sql + 02_data.sql)
#   .\run.ps1 stop             stop and remove the containers and the database
# The database is reset before web, runner and test, so the output is the same every time.
# $env:TOMCAT_PORT = "8090" before .\run.ps1 web uses another port if 8081 is taken ($env:DB_PORT the same for 5435).
param(
    [string]$Command = "web",
    [string]$Step = ""
)
# No $ErrorActionPreference = "Stop": Windows PowerShell 5.1 would treat docker's progress output on stderr
# as an error. Each step's exit code is checked instead.
$script:code = 0

function Invoke-Step([scriptblock]$Block) {
    if ($script:code -ne 0) { return }
    & $Block
    $script:code = $LASTEXITCODE
}

function Write-Title([string]$Text) {
    Write-Host ""
    Write-Host "== $Text ==" -ForegroundColor Cyan
}

# the status line and Location of an answer (redirects are not followed: they are the point here)
function Show-Status([string[]]$CurlArgs) {
    curl.exe -sS -i @CurlArgs | Select-String -Pattern '^(HTTP/|[Ll]ocation:)' | ForEach-Object { $_.Line }
}

function Reset-Database {
    Invoke-Step { docker compose up -d --wait db }
    Invoke-Step { docker compose exec -T db psql -q -v ON_ERROR_STOP=1 -U postgres -d pharmacy_database `
        -f /docker-entrypoint-initdb.d/01_schema.sql -f /docker-entrypoint-initdb.d/02_data.sql }
    if ($script:code -eq 0) {
        $dbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "5435" }
        Write-Host "Database pharmacy_database is reset (port $dbPort)"
    }
}

function Invoke-Maven([string[]]$MavenArgs) {
    Push-Location FinalProject
    try { Invoke-Step { .\mvnw.cmd -B -q @MavenArgs } } finally { Pop-Location }
}

# the medicine rows of user-medicines.jsp as the browser sends them: one rowId + quantity per row (ids 1-6)
function Get-CartForm([string]$Id, [string]$Quantity) {
    $form = @("-d", "medicineId=$Id")
    foreach ($row in 1..6) {
        $q = if ("$row" -eq $Id) { $Quantity } else { "0" }
        $form += @("-d", "rowId=$row", "-d", "quantity=$q")
    }
    return $form
}

$port = if ($env:TOMCAT_PORT) { $env:TOMCAT_PORT } else { "8081" }
$url = "http://localhost:$port"
# programs outside Docker (the tests, Runner) reach the database on the published port
$dbUrl = "-Ddb.url=jdbc:postgresql://localhost:" + $(if ($env:DB_PORT) { $env:DB_PORT } else { "5435" }) + "/pharmacy_database"

# Java, curl and psql print UTF-8; let the console decode it as UTF-8 too (restored at the end)
$originalEncoding = [Console]::OutputEncoding
try { [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding $false } catch { }

# run from the repository folder, and return the caller to their folder at the end
Push-Location $PSScriptRoot
$tmp = Join-Path ([IO.Path]::GetTempPath()) ("pharmacy-" + [Guid]::NewGuid().ToString("N"))
try {
    switch ($Command) {
        "web" {
            Invoke-Maven @("package", "-DskipTests")
            # fresh containers: the logs below then hold only this run (the data lives in a volume and is reset next)
            Invoke-Step { docker compose up -d --force-recreate --wait }
            Reset-Database
            if ($script:code -ne 0) { break }
            New-Item -ItemType Directory $tmp | Out-Null
            $user = Join-Path $tmp "anna.txt"
            $admin = Join-Path $tmp "admin.txt"

            Write-Title "curl $url/   (the start page: static, open to everybody)"
            curl.exe -sS "$url/" | Select-String -Pattern '<h1>|class="chain"' | ForEach-Object { $_.Line.Trim() }
            Write-Title "curl -i $url/user-medicines   (no session: AuthorizationFilter redirects to the login page)"
            Show-Status @("$url/user-medicines")
            Write-Title "curl -i -d eMail=anna@example.com -d password=wrong $url/login"
            Show-Status @("-d", "eMail=anna@example.com", "-d", "password=wrong", "$url/login")
            Write-Title "curl -i -c anna.txt -d eMail=anna@example.com -d password=anna $url/login   (USER -> /user-medicines)"
            Show-Status @("-c", $user, "-d", "eMail=anna@example.com", "-d", "password=anna", "$url/login")
            Write-Title "curl -b anna.txt $url/user-medicines   (the session cookie JSESSIONID lets the request through)"
            curl.exe -sS -b $user "$url/user-medicines" | Select-String -Pattern '\$ <br>' | ForEach-Object { $_.Line.Trim() }
            $cart = Get-CartForm "2" "3"
            Write-Title ("curl -b anna.txt " + ($cart -join " ") + " $url/user-cart   (3 x Ibuprofen)")
            $page = (curl.exe -sS -b $user @cart "$url/user-cart") -join " "
            [regex]::Matches(($page -replace '\s+', ' '), 'Ibuprofen[^<]*<br>[^<]*|Total price: [0-9]+\$') | ForEach-Object { $_.Value }
            Write-Title "curl -i -b anna.txt -d submitReject=2 $url/final-servlet   (Submit buying)"
            Show-Status @("-b", $user, "-d", "submitReject=2", "$url/final-servlet")
            Write-Title "curl -b anna.txt $url/user-medicines   (the message from the session, shown once)"
            curl.exe -sS -b $user "$url/user-medicines" | Select-String -SimpleMatch 'Successful purchase' | ForEach-Object { $_.Line.Trim() }
            Write-Title "curl -i -b anna.txt $url/admin-medicines   (a USER on an ADMIN page: AuthorizationFilter says no)"
            Show-Status @("-b", $user, "$url/admin-medicines")

            Write-Title "curl -i -c admin.txt -d eMail=admin@example.com -d password=admin $url/login   (ADMIN -> /admin)"
            Show-Status @("-c", $admin, "-d", "eMail=admin@example.com", "-d", "password=admin", "$url/login")
            # "Analgin" / "Belarus" in Cyrillic: this script is ASCII-only (Windows PowerShell 5.1 reads scripts without
            # a BOM as ANSI), and curl.exe would turn Cyrillic arguments into "??????" - so the words are built from
            # character codes and sent from UTF-8 files
            $name = -join [char[]](0x410, 0x43D, 0x430, 0x43B, 0x44C, 0x433, 0x438, 0x43D)
            $country = -join [char[]](0x411, 0x435, 0x43B, 0x430, 0x440, 0x443, 0x441, 0x44C)
            $utf8 = New-Object System.Text.UTF8Encoding $false
            $nameFile = Join-Path $tmp "name.txt"
            $countryFile = Join-Path $tmp "country.txt"
            [IO.File]::WriteAllText($nameFile, $name, $utf8)
            [IO.File]::WriteAllText($countryFile, $country, $utf8)
            Write-Title "curl -b admin.txt --data-urlencode name=$name ... -d price=4 -d medicineAddition $url/admin-medicines"
            curl.exe -sS -b $admin --data-urlencode "name@$nameFile" --data-urlencode "countryOfProduction@$countryFile" `
                -d price=4 -d medicineAddition=medicineAddition "$url/admin-medicines" |
                Select-String -Pattern "<p>|<h3>$name" | ForEach-Object { $_.Line.Trim() }
            Write-Title "curl -b admin.txt -H 'Referer: $url/admin-users' -d lang=ru_RU $url/locale, then /admin-users"
            Show-Status @("-b", $admin, "-H", "Referer: $url/admin-users", "-d", "lang=ru_RU", "$url/locale")
            curl.exe -sS -b $admin "$url/admin-users" | Select-String -Pattern '<h3>[^<]|Id:' | ForEach-Object { $_.Line.Trim() }
            Write-Title "curl -i -b admin.txt -X POST $url/logout"
            Show-Status @("-b", $admin, "-X", "POST", "$url/logout")

            Write-Title "PostgreSQL log: the SQL the application sent for these requests"
            docker compose logs --no-log-prefix db |
                Where-Object { $_.StartsWith('[PostgreSQL JDBC Driver]') -and $_ -match 'execute [^:]*:|DETAIL' -and $_ -notmatch 'SET (extra_float_digits|application_name)' } |
                ForEach-Object { $_.Replace('[PostgreSQL JDBC Driver] ', '') }
            Write-Title "Tomcat is running: open $url/ in a browser (.\run.ps1 stop stops it)"
        }
        "runner" {
            Reset-Database
            Invoke-Maven @("compile")
            if ($Step) { Invoke-Maven @("exec:java", $dbUrl, "-Dexec.args=$Step") } else { Invoke-Maven @("exec:java", $dbUrl) }
        }
        "test" {
            Reset-Database
            Push-Location FinalProject
            try { Invoke-Step { .\mvnw.cmd -B test $dbUrl } } finally { Pop-Location }
        }
        "reset" {
            Reset-Database
        }
        "stop" {
            Invoke-Step { docker compose down -v }
        }
        default {
            Write-Host "Unknown command: $Command (expected: web, runner [step], test, reset, stop)"
            $script:code = 1
        }
    }
} finally {
    Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue
    Pop-Location
    try { [Console]::OutputEncoding = $originalEncoding } catch { }
}
exit $script:code
