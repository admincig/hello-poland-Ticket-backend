# helloTicket (TST) – Backend API

Backendowa aplikacja Java odpowiedzialna za obsługę sprzedaży, rezerwacji
i zarządzania biletami w ekosystemie **Hello! Poland**.

Aplikacja działa jako REST API uruchamiane na serwerze **WildFly**
w kontenerze Docker i pełni rolę **wewnętrznego backendu biletowego**
(używanego m.in. przez backend HelloPoland).

---

## 🛠 Stos technologiczny

- **Język:** Java 17 (runtime) / Java 21 (build)
- **Serwer aplikacyjny:** WildFly
- **Konteneryzacja:** Docker
- **Baza danych:** PostgreSQL
- **Technologie:** JAX-RS (REST), JPA / Hibernate
- **Build tool:** Maven

---

## 🏗 Budowa projektu (Maven)

**Repozytorium:**  
https://github.com/cig-hellopoland/ht-backend

### Wymagania
- **JDK 21** – wymagane do procesu builda

### Profile Maven

Domyślnie aktywny jest profil `tst`, jednak zaleca się jawne określenie profilu:

**Środowisko testowe (TST):**
```bash
mvn -P tst clean package
```

**Środowisko produkcyjne (PROD):**
```bash
mvn -P prod clean package
```

**Wynik builda:**
```
target/helloticket.war
```

---

## ⚙️ Konfiguracja (Build-time vs Runtime)

| Cecha | Build-time | Runtime |
|------|-----------|---------|
| Pliki | `maven.build.[tst/prod].properties` | `local.runtime.properties` |
| Mechanizm | Maven resource filtering | bind-mount do kontenera |
| Zmiana | wymaga rebuild WAR | wymaga restartu kontenera |

---

## 🚀 Wdrożenie (środowisko TST)

**Serwer:** ns3084251  
**IP:** 145.239.133.24

### Układ katalogów na hoście

**WAR**
```
/srv/hpt/tst/war_file/helloticket.war
```

**Properties (runtime)**
```
/srv/hpt/tst/config/hpt-tst.config.properties
```

---

## 📁 DMS (Document Management System)

System plików wykorzystywany do przechowywania danych binarnych
(np. bilety, pliki pomocnicze).

**Docker volume (host):**
```
/var/lib/docker/volumes/HELLO_TICKET_DMS_TST/_data
```

**Punkt montowania w kontenerze:**
```
/DMS
```

---

## ▶️ Uruchomienie kontenera

```bash
docker rm -f hello_ticket_tst 2>/dev/null || true

docker run -d --name hello_ticket_tst   -p 8190:8080   -v /srv/hpt/tst/war_file/helloticket.war:/opt/jboss/wildfly/standalone/deployments/helloticket.war:ro   -v /srv/hpt/tst/config/hpt-tst.config.properties:/opt/jboss/wildfly/standalone/deployments/local.runtime.properties:ro   -v /var/lib/docker/volumes/HELLO_TICKET_DMS_TST/_data:/DMS:rw   --restart unless-stopped   hpt-backend:rel-YYYYMMDD_HHMMSS
```

**API dostępne lokalnie pod:**
```
http://127.0.0.1:8190/helloticket/v1/...
```

---

## 🔐 Autoryzacja i dostęp do endpointów

- Większość endpointów HelloTicket jest zabezpieczona
- Brak kontekstu autoryzacji skutkuje odpowiedzią:
```
HTTP 401 Unauthorized
```

---

## 🔄 Integracja z HelloPoland

- HelloTicket nie wystawia publicznego market API
- Publiczne endpointy rynku dostępne są w backendzie HelloPoland
- HelloPoland komunikuje się z HelloTicket po HTTP (wewnętrznie)

Powiązanie danych:
```
hellopoland.sight.hptid  →  helloticket.sight_event_id
```

---

## 🧪 Weryfikacja działania

```bash
docker ps | grep hello_ticket_tst
docker logs -n 200 hello_ticket_tst
```

```bash
curl -i http://127.0.0.1:8190/helloticket/v1/login
```

---

## 📝 Uwagi dodatkowe

- `git-commit-id-plugin` generuje plik `git.properties`
- Logi: `docker logs hello_ticket_tst`
- Redeploy przez restart kontenera

---

## 🔗 Linki

- Repozytorium: https://github.com/cig-hellopoland/ht-backend
- Wiki: https://wiki.coigdzie.pl/wiki/Hello!_Ticket:_API
