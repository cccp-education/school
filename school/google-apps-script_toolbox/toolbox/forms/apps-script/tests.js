/*=================================================================================*/
const ERROR_TESTS_MSG = "Erreur lors du chargement des tests : ";
const CANARY_MSG = 'Something unexpected happened in the canary';

/*=================================================================================*/

const tests = {
    logRunTests: ({ log }) => {
        log('Tests cases initiated.');
        tests.logCanaryTryCatch(log);
        tests.logCreateTestsContext(log);
        log('Behavioural Driven Development (BDD)')
        tests.canaryScenario(log);
        //lance l'action de démarrage
        onStartUp();
        tests.logFullfileRandomAnswers(log, PropertiesService);
        onSubmit();
    },
    /*
    Cette implémentation permet de décrire un scénario de test en trois parties :
    - **Given**: Définit le contexte initial du test et exécute une fonction de configuration.
    - **When**: Définit l'action à effectuer et exécute une fonction correspondante.
    - **Then**: Définit l'assertion à vérifier et exécute une fonction d'assertion.
    */
    TestScenario: {
        Given: (context, setupFunction, log) => ({
            When: (action, actionFunction) => ({
                Then: (assertion, assertionFunction) => {
                    log(`Scenario: ${context}`);
                    log(`Given ${context}`);
                    setupFunction();
                    log(`When ${action}`);
                    actionFunction();
                    log(`Then ${assertion}`);
                    assertionFunction();
                }
            })
        })
    },
    logCreateTestsContext: (log) => {
        try {
            //TODO: create world
        } catch (error) {
        }
    },
    logCanaryTryCatch: (log) => {
        try {
            log('Create tests context.');
            throw new Error(`${ERROR_TESTS_MSG}canary`);
        } catch (error) {
            if (error.message !== `${ERROR_TESTS_MSG}canary`) throw new Error(`${ERROR_TESTS_MSG}${CANARY_MSG}`);
        }
        Assertion.expectedException(tests.logCanaryTryCatch, Error, log, 'Testing canary expected exception');
    },
    // addRandomAnswers: () => {
    //     const form = FormApp.openById(FORM_ID);
    //     let multipleChoiceItems = form.getItems(FormApp.ItemType.MULTIPLE_CHOICE);
    //     let choices = multipleChoiceItems.asMultipleChoiceItem().getChoices();

    //     let nouvellesReponses = choices.map(function (choice) {
    //         return Array.from({length: nombreDeReponses}, function () {
    //             let randomChoiceIndex = Math.floor(Math.random() * choices.length);
    //             return choices[randomChoiceIndex].getValue();
    //         });
    //     }).flat();

    //     nouvellesReponses.forEach(function (newChoice) {
    //         multipleChoiceItem.asMultipleChoiceItem().createChoice(newChoice);
    //     });
    // }
    // ,
    logFullfileRandomAnswers: (log, props = PropertiesService) => {
        log("Fullfile random answers : ");
        log(EvaluationManager.getEvaluation(props)['sheetId']);
        log(EvaluationManager.getEvaluation(props)['form']);
        //    EvaluationManager.saveEvaluation()
        //        tests.addRandomAnswers();
    },
    /**
     {sheetId=1qv_4HN-MM6OhRG0W1m7RFJ8Ryu0Ob1DapjwSIiBk9k4, sheetData=[[Question, Propositions, Correction, Explication], [Qu'est-ce que Google Form Script ?, "Un service de traitement de texte en ligne", "Un langage de programmation pour automatiser des actions dans Google Forms", "Un outil de conception graphique pour formulaires", 1.0, Google Form Script est un langage de programmation permettant d'automatiser des actions dans Google Forms à l'aide de scripts associés.]]], form={}}
     */
    canaryScenario: (log) => {
        tests.TestScenario.Given("a user is logged in", () => {
            // Setup function to log in a user
            log("User is logged in");
        }, log).When("the user clicks on the logout button", () => {
            // Action function to simulate clicking on the logout button
            log("User clicks on the logout button");
        }).Then("the user should be logged out", () => {
            // Assertion function to check if the user is logged out
            log("User is logged out");
        });
    },
    logEvaluation: (evaluation, { log }) => {
        log(`evaluation["sheetId"] : ${evaluation["sheetId"]}`);
        log(`evaluation["form"] : ${evaluation["form"]}`);
        log(`evaluation["sheetData"] : ${evaluation["sheetData"]}`);
    },
}


/*=================================================================================*/
