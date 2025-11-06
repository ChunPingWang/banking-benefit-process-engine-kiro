Feature: Simple Test Scenario
  In order to verify Cucumber test framework works properly
  As a developer
  I want to execute basic test scenarios

  Scenario: System initialization test
    Given the system has initialized test data
    And audit tracking mechanism is enabled
    And external system mock service is ready
    When I check the system status
    Then the system should be ready for testing

  Scenario: Basic promotion evaluation test
    Given the system has initialized test data
    And customer "CUST001" has annual income of 1000000 yuan
    And customer has account type "VIP"
    When customer submits promotion evaluation request
    Then system should return promotion result
    And audit trail should be recorded