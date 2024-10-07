#language: fr
#noinspection CucumberUndefinedStep
Fonctionnalité: Inscription d'un compte utilisateur.

  Contexte:
    Etant donné une liste de login, email, password, firstName, lastName

      | login | email          | password | firstName | lastName |
      | admin | admin@acme.com | admin    | admin     | admin    |
      | user  | user@acme.com  | user     | user      | user     |


  Scénario: Création d'un nouveau compte utilisateur.
    Etant donné l'utilisateur de la liste qui a pour login "user"
    Quand on envoie la requête d'inscription de "user"
    Alors le résultat est la création d'un nouveau compte non activé