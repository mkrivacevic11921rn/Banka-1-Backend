Feature: Upravljanje transakcijama korisnika

  Scenario: Pregled transakcija bez duplikata za interne transfere
    Given korisnik sa ID-em 1 ima naloge i izvršen interni transfer između svojih naloga
    When pozove endpoint GET /transactions/1
    Then dobija listu sa 1 transakcijom za taj transfer
