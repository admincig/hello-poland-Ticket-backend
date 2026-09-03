# Changelog — Hello Ticket Backend

Każda zmiana numeru `<version>` w `pom.xml` wymaga dodania odpowiadającej jej sekcji
w tym pliku. Zmiany przygotowywane do następnego wydania zapisujemy w sekcji
`Unreleased`, a podczas podnoszenia wersji przenosimy je pod numer zgodny z POM-em.

## Unreleased

## 2.0.20.4 — 2026-09-02

### Zmieniono

- Wiadomości z wygenerowanymi hasłami bileterów partnera trafiają do konfigurowalnej
  skrzynki technicznej i respektują `mail.redirect.all.to` na TST.
- Domyślna techniczna kopia wiadomości `mail.ticket.copy` wskazuje
  `orders@hello-poland.pl`.

## 2.0.20.3 — 2026-08-30

### Dodano

- Endpoint HelpDesku do aktualizacji danych partnera w HT.
- Opcjonalne przekierowanie wiadomości na adres ustawiony we właściwości
  `mail.redirect.all.to`, przeznaczone między innymi do bezpiecznych testów wysyłki.

### Zmieniono

- Aktualizacja partnera zapisuje atomowo nazwę i e-mail w `PARTNERS` oraz na głównym,
  ukrytym koncie partnera w `USERS`.
- Zmiana loginu głównego konta partnera aktualizuje również adres powiadomień partnera.
- Walidację i zapis konfiguracji cyklicznych pul biletowych, w tym częstotliwości i dni tygodnia.
- Obsługę kopii i technicznych powiadomień e-mail przy aktywnym przekierowaniu wiadomości.

### Naprawiono

- Rozjazd adresu logowania i adresu używanego przez HT do wysyłania potwierdzeń partnerowi.
- Widoczność wydarzeń posiadających cykliczną pulę, zanim zostaną utworzone jej konkretne wystąpienia.
- Komunikat błędu zwracany dla niedostępnego terminu biletu.
