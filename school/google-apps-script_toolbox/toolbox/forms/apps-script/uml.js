const FORM_ID = "19XrDcNxQnWj9AsLUnktTGlm8uV71VpWJku-BugrlC_8";
const ERROR_QUESTIONS_MSG = "Erreur lors du chargement des questions du formulaire : ";
const ERROR_EVALUATION_MSG = "Erreur lors du chargement de l'évaluation : ";
const ERROR_TESTS_MSG = "Erreur lors du chargement des tests : ";
const CANARY_MSG = 'Something unexpected happened in the canary';

/*=================================================================================*/

const onStartUp = () => EvaluationManager
    .matchError(EvaluationManager.Maybe.of(EvaluationManager.eval(FORM_ID)))
    .onNothing(({ log }) => log("L'évaluation n'a pas été correctement initialisée."))
    .onJust((evaluation) => { EvaluationManager.loadFormQuestions(evaluation); });

/*=================================================================================*/

const onSubmit = () => EvaluationManager
    .matchError(EvaluationManager.Maybe.of(EvaluationManager.getEvaluation(PropertiesService)))
    .onNothing(({ log }) => log("L'évaluation n'a pas été correctement remplie."))
    .onJust((evaluation) => { EvaluationManager.feedback(Logger, evaluation); });
/*=================================================================================*/
const check = () => tests.logRunTests(Logger);
/*=================================================================================*/
const EvaluationManager = {
    eval: (formId) => {
        let getFilesByName;
        ({ getFilesByName } = DriveApp.getFolderById(DriveApp.getFileById(formId).getParents().next().getId()));
        const { hasNext, next } = getFilesByName('eval.json');
        if (hasNext()) {
            let { getBlob } = next();
            return JSON.parse(getBlob().getDataAsString());
        } else {
            throw new Error('Le fichier eval.json n\'a pas été trouvé dans le même répertoire que le formulaire.');
        }
    },
    /*=================================================================================*/
    Maybe: {
        of: (value) => ({
            value,
            map: (fn) => (value ? EvaluationManager.Maybe.of(fn(value)) : EvaluationManager.Maybe.of(null)),
            flatMap: (fn) => (value ? fn(value) : EvaluationManager.Maybe.of(null)),
            isNothing: () => value === null || value === undefined,
            getOrElse: (defaultValue) => (EvaluationManager.Maybe.of(value).isNothing() ? defaultValue : value),
        }),
    },

    /*=================================================================================*/
    matchError: (maybeEvaluation) => ({
        onNothing: (fn) => (maybeEvaluation.isNothing() ? fn() : EvaluationManager
            .matchError(maybeEvaluation)),
        onJust: (fn) => (maybeEvaluation.isNothing() ? EvaluationManager
            .matchError(maybeEvaluation) : fn(maybeEvaluation.value)),
    }),
    /*=================================================================================*/

    loadFormQuestions: (evaluation) => {
        try {
            const form = FormApp.openById(FORM_ID);
            const existingQuestions = form.getItems(FormApp.ItemType.MULTIPLE_CHOICE);
            const existingTitles = existingQuestions.map(({ getTitle }) => getTitle());

            evaluation
                .map(({ Question, Choix }) => ({ Question, Choix })) // Correction ici
                .forEach(({ Question, Choix }) => {
                    if (!existingTitles.includes(Question)) {
                        EvaluationManager.addQuestionToForm(form, Question, Choix);
                        existingTitles.push(Question);
                    }
                });
        } catch (error) {
            throw new Error(`${ERROR_QUESTIONS_MSG}${error.message}`);
        }
    },


    /*=================================================================================*/
    addQuestionToForm: (form, Question, Choix) => {
        form.addMultipleChoiceItem()
            .setTitle(Question)
            .setChoiceValues(Choix)
            .setPoints(1)
            .setRequired(true);
    },

    /*=================================================================================*/
    feedback: ({ log }, evaluation) => {
        // Exemple de feedback basé sur les performances de l'utilisateur
        let score = 0;
        let totalQuestions = 0;
        let wrongAnswers = [];

        // Calculer le score de l'utilisateur et collecter les mauvaises réponses
        evaluation.forEach(({ Question, Choix, Reponse, Correction }) => {
            totalQuestions++;
            if (Reponse >= 0 && Reponse < Choix.length && Choix[Reponse]) {
                score++;
            } else {
                wrongAnswers.push({ Question, Correction });
            }
        });

        // Calculer le pourcentage de bonnes réponses
        let percentage = (score / totalQuestions) * 100;

        // Générer un feedback en fonction du score
        let feedbackMessage;
        if (percentage >= 70) {
            feedbackMessage = "Félicitations ! Vous avez bien réussi l'évaluation.";
        } else if (percentage >= 50) {
            feedbackMessage = "Vous avez réussi l'évaluation, mais vous pouvez vous améliorer.";
        } else {
            feedbackMessage = "Vous n'avez pas réussi l'évaluation. Veuillez réviser et essayer à nouveau.";
        }

        // Journaliser le feedback
        log(feedbackMessage);

        // Journaliser les corrections pour les mauvaises réponses
        wrongAnswers.forEach(({ Question, Correction }) => {
            log(`Question: ${Question}\nCorrection: ${Correction}\n`);
        });
    }
    ,
    /*feedback: ({ log }, evaluation) => {
        // Exemple de feedback basé sur les performances de l'utilisateur
        let score = 0;
        let totalQuestions = 0;
        let wrongAnswers = [];

        // Calculer le score de l'utilisateur et collecter les mauvaises réponses
        evaluation.forEach(({ Question, Choix, Reponse, Correction }) => {
            totalQuestions++;
            if (Reponse >= 0 && Reponse < Choix.length && Choix[Reponse]) {
                score++;
            } else {
                wrongAnswers.push({ Question, Correction });
            }
        });

        // Calculer le pourcentage de bonnes réponses
        let percentage = (score / totalQuestions) * 100;

        // Générer un feedback en fonction du score
        let feedbackMessage;
        if (percentage >= 70) {
            feedbackMessage = "Félicitations ! Vous avez bien réussi l'évaluation.";
        } else if (percentage >= 50) {
            feedbackMessage = "Vous avez réussi l'évaluation, mais vous pouvez vous améliorer.";
        } else {
            feedbackMessage = "Vous n'avez pas réussi l'évaluation. Veuillez réviser et essayer à nouveau.";
        }

        // Journaliser le feedback
        log(feedbackMessage);

        // Journaliser les corrections pour les mauvaises réponses
        wrongAnswers.forEach(({ Question, Correction }) => {
            log(`Question: ${Question}\nCorrection: ${Correction}\n`);
        });
    }*/



};


/*=================================================================================*/

const tests = {
    logRunTests: ({ log }) => {
        log('Tests cases initiated.');
        tests.logCanaryTryCatch(log);
        tests.logCreateTestsContext(log);
        log('Behavioural Driven Development (BDD)')
        tests.canaryScenario(log);//Question Choix Reponse Correction
        EvaluationManager.eval(FORM_ID).forEach(({ Question }) => { log(Question) });
        onStartUp();
        //tests.logFullfileRandomAnswers(log, PropertiesService);
        //onSubmit();
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
        //        tests.addRandomAnswers();
    },
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


const Assertion = {
    assertEquals: (actual, expected, log, message = '') => {
        if (actual !== expected) log(`Assertion Failed: ${message}. Expected ${expected}, but got ${actual}`);
    },
    assertTrue: (condition, log, message = '') => {
        if (!condition) log(`Assertion Failed: ${message}. Expected true, but got false`);
    },
    assertFalse: (condition, log, message = '') => {
        if (condition) log(`Assertion Failed: ${message}. Expected false, but got true`);
    },
    assertUndefined: (value, log, message = '') => {
        if (typeof value !== 'undefined') log(`Assertion Failed: ${message}. Expected undefined, but got ${value}`);
    },
    assertNotNull: (value, log, message = '') => {
        if (value === null) log(`Assertion Failed: ${message}. Expected not null, but got null`);
    },
    assertNotNaN: (value, log, message = '') => {
        if (isNaN(value)) log(`Assertion Failed: ${message}. Expected not NaN, but got NaN`);
    },
    expectedException: (func, expectedError, log, message = '') => {
        try {
            func();
            log(`Assertion Failed: ${message}. Expected an exception of type ${expectedError}`);
        } catch (error) {
            if (!(error instanceof expectedError)) log(`Assertion Failed: ${message}. Expected an exception of type ${expectedError}, but got ${error}`);
        }
    }
};
/*=================================================================================*/
