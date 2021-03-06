Feature: Database

  Background:
    Backend performs CRUD actions to the database

    Scenario:
      Given I want to create a new user
      When I enter my first name as "Taehyung", my last name as "Kim", and my password as "password13456997"
      Then A newly created user is created in the database

    Scenario:
      Given I want to delete a current user
      When I enter my client card as 4519011123012416
      Then The database should delete my record

    Scenario:
      Given I want to update a user's information
      When I enter my client card as 4519011123012419 and I enter my new first name as "Jihoon" and my last name as "Lee" and my password as "LeeJiHoon"
      Then The database should update my record

