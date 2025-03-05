Feature: Internal Money Transfer

  Scenario: Successful internal transfer
    Given an account with id 1 and balance 1000.0
    And an account with id 2 and balance 500.0
    And a pending internal transfer of 200.0 from account 1 to account 2
    When the transfer is processed
    Then the balance of account 1 should be 800.0
    And the balance of account 2 should be 700.0
    And the transfer status should be COMPLETED

  Scenario: Internal transfer fails due to insufficient funds
    Given an account with id 1 and balance 100.0
    And an account with id 2 and balance 500.0
    And a pending internal transfer of 200.0 from account 1 to account 2
    When the transfer is processed
    Then the transfer should fail with message "Insufficient funds"
