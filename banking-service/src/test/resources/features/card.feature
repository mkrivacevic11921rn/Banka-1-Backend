Feature: Customer Cards

    Scenario: Customer creates a new card
        Given customer is logged into the banking portal for cards
        And customer navigates to card page
        When customer fills out the card form
        And customer presses the Continue button for cards
        Then customer should see a success message for cards