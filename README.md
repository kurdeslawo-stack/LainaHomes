# LainaHomes

Siostrzany frontend GUI dla istniejących home'ów EssentialsX na Paper 26.2.
EssentialsX pozostaje jedynym źródłem lokalizacji oraz całej logiki teleportacji.

## Zachowanie

- `/home` bez argumentów otwiera GUI.
- `/homes` i `/homegui` również otwierają GUI.
- `/home <nazwa>` nie jest przechwytywane i działa jak wcześniej w EssentialsX.
- LPM wykonuje jako gracz `/home <nazwa>`; addon nie używa `teleport` ani `teleportAsync`.
- PPM przełącza ulubione. Ulubione i opisy są zapisane osobno w
  `plugins/LainaHomes/gui-data.yml`.
- Shift + PPM rozpoczyna bezpieczną edycję opisu przez następną wiadomość na czacie.
- `/homegui description <home> <tekst>` ustawia opis, a `-` go usuwa.
- Usunięte lub przemianowane home'y są automatycznie czyszczone z danych dodatku.

## Budowanie

```powershell
mvn clean test
mvn package
```

Wynik: `target/LainaHomes-0.1.0.jar`.
