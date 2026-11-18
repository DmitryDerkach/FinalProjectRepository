"""2026: records real HTTP exchanges for the "under the hood" page (site/index.html).

Needs ./run.sh web to have run (Tomcat on port 8081, PostgreSQL with log_statement=all). Resets the database
first, then sends every request over a plain TCP socket - three "browsers" (a guest, the customer Anna, the
administrator), each with its own session cookie - and saves, for each request:
  - the exact bytes of the request and the response;
  - the SQL statements PostgreSQL received while the request was handled (from the container log);
and, once, the Java source that Tomcat's Jasper generated from WEB-INF/jsp/login.jsp.
Everything goes to site/captures.js. The GitHub Pages job in .github/workflows/ci.yml runs it on every deploy,
so the page always shows what the current code does.

Usage: python3 site/capture.py [tomcat_port]
"""
import datetime
import json
import os
import pathlib
import re
import socket
import subprocess
import sys
import time
import urllib.parse

HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parent
PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8081
AGENT = "User-Agent: capture.py (pharmacy)\r\n"
APP_LOG_PREFIX = "[PostgreSQL JDBC Driver] "
JSP_SOURCE = "/usr/local/tomcat/work/Catalina/localhost/ROOT/org/apache/jsp/WEB_002dINF/jsp/login_jsp.java"


def exchange(request: bytes) -> bytes:
    """Sends the raw request and reads until the server closes the connection."""
    with socket.create_connection(("localhost", PORT), timeout=10) as sock:
        sock.sendall(request)
        chunks = []
        while True:
            chunk = sock.recv(65536)
            if not chunk:
                return b"".join(chunks)
            chunks.append(chunk)


def compose(*args):
    return subprocess.run(["docker", "compose", *args], cwd=ROOT, capture_output=True, text=True,
                          encoding="utf-8", errors="replace", timeout=120).stdout


def reset_db():
    subprocess.run(["docker", "compose", "exec", "-T", "db", "psql", "-q", "-v", "ON_ERROR_STOP=1", "-U", "postgres",
                    "-d", "pharmacy_database", "-f", "/docker-entrypoint-initdb.d/01_schema.sql",
                    "-f", "/docker-entrypoint-initdb.d/02_data.sql"], cwd=ROOT, check=True, capture_output=True,
                   env={**os.environ, "MSYS_NO_PATHCONV": "1"})


def db_log():
    return compose("logs", "--no-log-prefix", "db").splitlines()


def sql_since(lines_before):
    """SQL that the application sent after the first lines_before lines of the database log."""
    time.sleep(0.4)  # PostgreSQL writes its log asynchronously
    out = []
    for line in db_log()[lines_before:]:
        if not line.startswith(APP_LOG_PREFIX):
            continue
        text = line[len(APP_LOG_PREFIX):]
        # pgjdbc's own settings on every new connection - not the application's SQL
        if re.search(r"SET (extra_float_digits|application_name)", text):
            continue
        text = re.sub(r"^LOG:\s+execute <unnamed>: ", "", text)
        text = re.sub(r"^DETAIL:\s+Parameters: ", "  -- parameters: ", text)
        if text.startswith("  -- parameters") or not text.startswith(("LOG:", "DETAIL:")):
            out.append(text)
    return out


def commit():
    sha = os.environ.get("GITHUB_SHA")
    if sha:
        return sha[:7]
    try:
        return subprocess.run(["git", "rev-parse", "--short", "HEAD"], cwd=ROOT, capture_output=True,
                              text=True, timeout=10).stdout.strip()
    except (OSError, subprocess.SubprocessError):
        return ""


class Browser:
    """One browser: keeps the JSESSIONID cookie that Tomcat gives it, like a real one."""

    def __init__(self):
        self.cookie = ""

    def request(self, method, path, form=None, extra=""):
        cookie = f"Cookie: {self.cookie}\r\n" if self.cookie else ""
        if method == "GET":
            return (f"GET {path} HTTP/1.1\r\nHost: localhost:{PORT}\r\n{AGENT}Accept: text/html\r\n{cookie}{extra}"
                    f"Connection: close\r\n\r\n").encode()
        body = urllib.parse.urlencode(form or [], encoding="utf-8").encode()
        return (f"POST {path} HTTP/1.1\r\nHost: localhost:{PORT}\r\n{AGENT}{cookie}{extra}"
                f"Content-Type: application/x-www-form-urlencoded\r\nContent-Length: {len(body)}\r\n"
                f"Connection: close\r\n\r\n").encode() + body

    def remember(self, response: bytes):
        match = re.search(rb"Set-Cookie: (JSESSIONID=[^;\r\n]+)", response)
        if match:
            self.cookie = match.group(1).decode()


def cart_form(quantities, checked):
    """The fields of user-medicines.jsp: the checked ids, then rowId + quantity for each of the six rows."""
    form = [("medicineId", c) for c in checked]
    for row, q in enumerate(quantities, 1):
        form += [("rowId", str(row)), ("quantity", q)]
    return form


def main():
    reset_db()
    scenarios = []
    guest, anna, admin = Browser(), Browser(), Browser()

    def record(sid, group, title, handler, browser, method, path, form=None, extra=""):
        request = browser.request(method, path, form, extra)
        before = len(db_log())
        response = exchange(request)
        browser.remember(response)
        scenarios.append({
            "id": sid, "group": group, "title": title, "handler": handler,
            "request": request.decode("utf-8", errors="replace"),
            "response": response.decode("utf-8", errors="replace"),
            "sql": sql_since(before),
        })
        status_line = response.split(b"\r\n", 1)[0].decode()
        print(f"{sid:18} {status_line}")
        return response

    # the door: what the filters let through
    record("start", "door", "GET / - the start page", "DefaultServlet (index.html)", guest, "GET", "/")
    record("guest-blocked", "door", "GET /user-medicines without a session", "AuthorizationFilter -> /login",
           guest, "GET", "/user-medicines")
    record("login-form", "door", "GET /login - the login form", "LoginServlet.doGet -> login.jsp", guest, "GET", "/login")

    # login: the session gets a user
    record("login-wrong", "login", "POST /login with a wrong password", "LoginServlet -> /login?error",
           guest, "POST", "/login", [("eMail", "anna@example.com"), ("password", "wrong")])
    anna.cookie = guest.cookie  # Anna uses the same browser: the session from the login page goes on
    record("login-user", "login", "POST /login as the customer Anna", "LoginServlet -> session -> /user-medicines",
           anna, "POST", "/login", [("eMail", "anna@example.com"), ("password", "anna")])
    record("login-admin", "login", "POST /login as the administrator", "LoginServlet -> session -> /admin",
           admin, "POST", "/login", [("eMail", "admin@example.com"), ("password", "admin")])

    # roles
    record("user-on-admin", "roles", "GET /admin-medicines as the customer", "AuthorizationFilter -> /user-medicines",
           anna, "GET", "/admin-medicines")
    record("admin-medicines", "roles", "GET /admin-medicines as the administrator",
           "AdminMedicineServlet -> admin-medicines.jsp", admin, "GET", "/admin-medicines")

    # shopping
    record("medicines", "shop", "GET /user-medicines - the shop window", "UserMedicineServlet -> user-medicines.jsp",
           anna, "GET", "/user-medicines")
    record("cart", "shop", "POST /user-cart - 3 x Ibuprofen + 2 x Validol", "UserMedicineCartServlet -> user-medicine-cart.jsp",
           anna, "POST", "/user-cart", cart_form(["0", "3", "0", "0", "0", "2"], ["2", "6"]))
    record("buy", "shop", "POST /final-servlet - Submit buying", "FinalServlet -> /user-medicines",
           anna, "POST", "/final-servlet", [("submitReject", "2")])
    record("after-buy", "shop", "GET /user-medicines - the message, once", "UserMedicineServlet -> user-medicines.jsp",
           anna, "GET", "/user-medicines")
    record("cart-wrong", "shop", "POST /user-cart - a quantity in the wrong row",
           "UserMedicineCartServlet -> /user-medicines", anna, "POST", "/user-cart",
           cart_form(["0", "0", "0", "0", "0", "5"], ["2"]))

    # the administrator changes the stock
    record("add", "admin", "POST /admin-medicines - add a Cyrillic name", "EncodingFilter + AdminMedicineServlet",
           admin, "POST", "/admin-medicines",
           [("name", "Корвалол"), ("countryOfProduction", "Беларусь"), ("price", "6"), ("medicineAddition", "medicineAddition")])
    record("add-wrong", "admin", "POST /admin-medicines - price \"abc\"", "AdminMedicineServlet -> the form again",
           admin, "POST", "/admin-medicines",
           [("name", "Aspirin"), ("countryOfProduction", "Germany"), ("price", "abc"), ("medicineAddition", "medicineAddition")])

    # registration
    record("register-wrong", "register", "POST /registration - an email that is already registered",
           "RegistrationServlet -> UserService (errors) -> registration.jsp", Browser(), "POST", "/registration",
           [("userName", "Anna"), ("eMail", "anna@example.com"), ("password", "x")])
    record("register", "register", "POST /registration - a new customer", "RegistrationServlet -> UserDao -> /login",
           Browser(), "POST", "/registration",
           [("userName", "Мария Иванова"), ("eMail", "maria@example.com"), ("password", "secret")])

    # language and logout
    record("locale", "lang", "POST /locale - switch to Russian", "LocaleServlet -> back to the page",
           admin, "POST", "/locale", [("lang", "ru_RU")], f"Referer: http://localhost:{PORT}/admin-users\r\n")
    record("users-ru", "lang", "GET /admin-users - now in Russian", "AdminUserServlet -> admin-users.jsp",
           admin, "GET", "/admin-users")
    record("logout", "lang", "POST /logout", "LogoutServlet: session.invalidate()", admin, "POST", "/logout")
    record("after-logout", "lang", "GET /admin after the logout", "AuthorizationFilter -> /login",
           admin, "GET", "/admin")

    jsp_java = compose("exec", "-T", "tomcat", "cat", JSP_SOURCE)
    data = {
        "captured_at": datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%d %H:%M UTC"),
        "where": "GitHub Actions" if os.environ.get("GITHUB_ACTIONS") else "a local run",
        "commit": commit(),
        "scenarios": scenarios,
        "jsp_java": jsp_java,
    }
    out = HERE / "captures.js"
    out.write_text("// generated by site/capture.py - real requests, responses and SQL, do not edit by hand\n"
                   "window.CAPTURES = " + json.dumps(data, ensure_ascii=False, indent=1) + ";\n",
                   encoding="utf-8", newline="\n")
    print(f"saved {len(scenarios)} exchanges, {sum(len(s['sql']) for s in scenarios)} SQL lines and "
          f"{len(jsp_java.splitlines())} lines of login_jsp.java to {out}")


if __name__ == "__main__":
    main()
