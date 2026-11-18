#!/usr/bin/env bash
# 2026: one-command run of the pharmacy (Linux, macOS, Git Bash on Windows). Needs JDK 11+ and Docker.
#   ./run.sh                  the same as ./run.sh web
#   ./run.sh web              build the application, start PostgreSQL + Tomcat 9 in Docker with a fresh database,
#                             walk through the shop with curl (filters, login, roles, cart, admin, language),
#                             show the SQL the application sent, and leave everything running:
#                             open http://localhost:8081/ in a browser
#   ./run.sh runner [step]    the console Runner (DAO without a server): all steps, or one of
#                             medicines, users, saveUser, deleteMedicine, findMedicine
#   ./run.sh test             JUnit tests: DAO on the database, filters, servlets and JSP on an embedded Tomcat
#   ./run.sh reset            recreate the tables and the data (01_schema.sql + 02_data.sql)
#   ./run.sh stop             stop and remove the containers and the database
# The database is reset before web, runner and test, so the output is the same every time.
# TOMCAT_PORT=8090 ./run.sh web uses another port if 8081 is taken (DB_PORT the same for 5435).
set -euo pipefail
cd "$(dirname "$0")"

PORT="${TOMCAT_PORT:-8081}"
URL="http://localhost:$PORT"
APP=FinalProject
# programs outside Docker (the tests, Runner) reach the database on the published port
DB_URL="jdbc:postgresql://localhost:${DB_PORT:-5435}/pharmacy_database"

# Git Bash would rewrite container paths (/docker-entrypoint-initdb.d) into Windows paths; docker commands only
docker_cmd() { MSYS_NO_PATHCONV=1 docker "$@"; }

build() { (cd "$APP" && ./mvnw -B -q "$@"); }

title() { printf '\n\033[1;36m== %s ==\033[0m\n' "$*"; }

# the status line and Location of an answer (redirects are not followed: they are the point here)
status() { tr -d '\r' | grep -iE '^(HTTP/|location:)' || true; }

reset_db() {
  docker_cmd compose up -d --wait db
  docker_cmd compose exec -T db psql -q -v ON_ERROR_STOP=1 -U postgres -d pharmacy_database \
    -f /docker-entrypoint-initdb.d/01_schema.sql -f /docker-entrypoint-initdb.d/02_data.sql
  echo "Database pharmacy_database is reset (port ${DB_PORT:-5435})"
}

# the medicine rows of user-medicines.jsp as the browser sends them: one rowId + quantity per row (ids 1-6),
# $1 = the id to buy, $2 = how many
cart_form() {
  local id
  printf -- '-d medicineId=%s' "$1"
  for id in 1 2 3 4 5 6; do
    if [ "$id" = "$1" ]; then printf -- ' -d rowId=%s -d quantity=%s' "$id" "$2"; else printf -- ' -d rowId=%s -d quantity=0' "$id"; fi
  done
}

web() {
  build package -DskipTests
  # fresh containers: the logs below then hold only this run (the data lives in a volume and is reset next)
  docker_cmd compose up -d --force-recreate --wait
  reset_db
  local tmp user admin
  tmp="$(mktemp -d)"
  user="$tmp/anna.txt"
  admin="$tmp/admin.txt"
  trap 'rm -rf "$tmp"' RETURN

  title "curl $URL/   (the start page: static, open to everybody)"
  curl -sS "$URL/" | grep -E '<h1>|class="chain"' | sed 's/^ *//'
  title "curl -i $URL/user-medicines   (no session: AuthorizationFilter redirects to the login page)"
  curl -sS -i "$URL/user-medicines" | status
  title "curl -i -d eMail=anna@example.com -d password=wrong $URL/login"
  curl -sS -i -d eMail=anna@example.com -d password=wrong "$URL/login" | status
  title "curl -i -c anna.txt -d eMail=anna@example.com -d password=anna $URL/login   (USER -> /user-medicines)"
  curl -sS -i -c "$user" -d eMail=anna@example.com -d password=anna "$URL/login" | status
  title "curl -b anna.txt $URL/user-medicines   (the session cookie JSESSIONID lets the request through)"
  curl -sS -b "$user" "$URL/user-medicines" | grep -E '\$ <br>' | sed 's/^ *//'
  title "curl -b anna.txt $(cart_form 2 3) $URL/user-cart   (3 x Ibuprofen)"
  # shellcheck disable=SC2046
  curl -sS -b "$user" $(cart_form 2 3) "$URL/user-cart" | tr -s ' \n' ' ' | grep -oE 'Ibuprofen[^<]*<br>[^<]*|Total price: [0-9]+\$' || true
  title "curl -i -b anna.txt -d submitReject=2 $URL/final-servlet   (Submit buying)"
  curl -sS -i -b "$user" -d submitReject=2 "$URL/final-servlet" | status
  title "curl -b anna.txt $URL/user-medicines   (the message from the session, shown once)"
  curl -sS -b "$user" "$URL/user-medicines" | grep -F 'Successful purchase' | sed 's/^ *//'
  title "curl -i -b anna.txt $URL/admin-medicines   (a USER on an ADMIN page: AuthorizationFilter says no)"
  curl -sS -i -b "$user" "$URL/admin-medicines" | status

  title "curl -i -c admin.txt -d eMail=admin@example.com -d password=admin $URL/login   (ADMIN -> /admin)"
  curl -sS -i -c "$admin" -d eMail=admin@example.com -d password=admin "$URL/login" | status
  # the name goes through a file: curl on Windows would turn a Cyrillic command-line argument into "??????"
  printf '%s' 'Анальгин' > "$tmp/name.txt"
  printf '%s' 'Беларусь' > "$tmp/country.txt"
  # "name@file" is not rewritten into a Windows path by Git Bash (curl.exe would not find /tmp/...): cygpath does it
  local files="$tmp"
  if command -v cygpath > /dev/null; then files="$(cygpath -m "$tmp")"; fi
  title "curl -b admin.txt --data-urlencode name=Анальгин ... -d price=4 -d medicineAddition $URL/admin-medicines"
  curl -sS -b "$admin" --data-urlencode "name@$files/name.txt" --data-urlencode "countryOfProduction@$files/country.txt" \
    -d price=4 -d medicineAddition=medicineAddition "$URL/admin-medicines" | grep -E '<p>|<h3>Анальгин' | sed 's/^ *//'
  title "curl -b admin.txt -H 'Referer: $URL/admin-users' -d lang=ru_RU $URL/locale, then /admin-users"
  curl -sS -i -b "$admin" -H "Referer: $URL/admin-users" -d lang=ru_RU "$URL/locale" | status
  curl -sS -b "$admin" "$URL/admin-users" | grep -E '<h3>[^<]|Id:' | sed 's/^ *//'
  title "curl -i -b admin.txt -X POST $URL/logout"
  curl -sS -i -b "$admin" -X POST "$URL/logout" | status

  title "PostgreSQL log: the SQL the application sent for these requests"
  docker_cmd compose logs --no-log-prefix db | grep -F '[PostgreSQL JDBC Driver]' | grep -E 'execute [^:]*:|DETAIL' \
    | grep -v -E 'SET (extra_float_digits|application_name)' | sed -e 's/^\[PostgreSQL JDBC Driver\] //' || true
  title "Tomcat is running: open $URL/ in a browser (./run.sh stop stops it)"
}

runner() {
  reset_db
  build compile
  if [ $# -gt 0 ]; then
    build exec:java -Ddb.url="$DB_URL" -Dexec.args="$1"
  else
    build exec:java -Ddb.url="$DB_URL"
  fi
}

case "${1:-web}" in
  web)    web ;;
  runner) shift; runner "$@" ;;
  test)   reset_db; (cd "$APP" && ./mvnw -B test -Ddb.url="$DB_URL") ;;
  reset)  reset_db ;;
  stop)   docker_cmd compose down -v ;;
  *)
    echo "Unknown command: $1 (expected: web, runner [step], test, reset, stop)" >&2
    exit 1
    ;;
esac
