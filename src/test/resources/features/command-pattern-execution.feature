Feature: Command Pattern Execution Test

  Background:
    Given the system is initialized
    And test data is prepared

  Scenario: SpEL condition command executes successfully
    Given the system is configured with SpEL condition command "#creditScore > 700"
    And customer credit score is 750
    When SpEL condition command is executed
    Then the command should execute successfully
    And the return result should be true
    And the command type should be "SPEL_CONDITION"

  Scenario: SpEL condition command condition not met
    Given the system is configured with SpEL condition command "#creditScore > 700"
    And customer credit score is 650
    When SpEL condition command is executed
    Then the command should execute successfully
    And the return result should be false

  Scenario: SpEL calculation command executes promotion calculation
    Given the system is configured with SpEL calculation command "#accountBalance * 0.05"
    And customer account balance is 500000 yuan
    When SpEL calculation command is executed
    Then the command should execute successfully
    And the return promotion result should contain discount amount 25000 yuan
    And the promotion name should be "SpEL Calculation Promotion"

  Scenario: SpEL calculation command uses configuration parameters
    Given the system is configured with SpEL calculation command "#accountBalance * #param_discountRate"
    And configuration parameter "discountRate" is 0.03
    And configuration parameter "promotionName" is "Custom Promotion"
    And customer account balance is 500000 yuan
    When SpEL calculation command is executed
    Then the command should execute successfully
    And the return promotion result should contain discount amount 15000 yuan
    And the promotion name should be "Custom Promotion"

  Scenario: SpEL command handles invalid expression
    Given the system is configured with SpEL condition command "#invalidProperty"
    And customer credit score is 750
    When SpEL condition command is executed
    Then the command should execute with failure
    And the error message should contain "SpEL condition evaluation failed"

  Scenario: Drools rule command executes high-value customer judgment
    Given the system is configured with Drools rule command "High Value Customer Judgment Rule"
    And Drools rule content is:
      """
      rule "HighValueCustomer"
      when
          $customer : Customer(annualIncome > 1000000, creditScore > 700)
      then
          $customer.setHighValue(true);
      end
      """
    And customer annual income is set to 2000000 yuan
    And customer credit score is 750
    When Drools rule command is executed
    Then the command should execute successfully
    And the return result should be true

  Scenario: Drools rule command calculates promotion amount
    Given the system is configured with Drools calculation command "Promotion Amount Calculation Rule"
    And Drools rule content is:
      """
      rule "CalculateDiscount"
      when
          $customer : Customer(accountBalance > 100000)
      then
          $customer.setDiscountAmount($customer.getAccountBalance() * 0.02);
      end
      """
    And customer account balance is 500000 yuan
    When Drools calculation command is executed
    Then the command should execute successfully
    And the return promotion result should contain discount amount 10000 yuan

  Scenario: External system command successful call
    Given the system is configured with external system condition command calling credit evaluation service
    And external system endpoint is "http://credit-service/evaluate"
    And external credit evaluation service returns result:
      | conditionResult | true |
      | creditLevel     | HIGH |
    When external system command is executed
    Then the command should execute successfully
    And the return result should be true