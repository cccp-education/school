const FORM_ID = '1cstDODm2LYnOnbDNEbuIbJaYNsR1L5VreSWlyvt5bVY';

const check = () => FormTests.logRunTests(Logger);

const onStartup = () => EvaluationManager.startEvaluation();

const onSubmit = () => EvaluationManager.submitEvaluation();

const dump = () => {
  const log = Logger.log;
  const {
    deleteAllResponses,
    getItems,
    deleteItem,
  } = FormApp.getActiveForm();
  log(`items length before dump: ${getItems().length}`);
  getItems().map(item => deleteItem(item));
  deleteAllResponses();
  log(`items length after dump: ${getItems().length}`);
};

const EvaluationManager = {
  QUESTIONS_KEY: 'questions',
  ERROR_QUESTIONS_MSG: `Erreur lors du chargement des questions du formulaire : `,
  ERROR_EVALUATION_MSG: `Erreur lors du chargement de l'évaluation : `,
  file: DriveApp.getFileById(FORM_ID),
  form: FormApp.openById(FORM_ID),
  /*------------------------------------*/
  Maybe: {
    of: (value) => ({
      value,
      map: (fn) => value ? EvaluationManager.Maybe.of(fn(value)) : EvaluationManager.Maybe.of(null),
      flatMap: (fn) => (value ? fn(value) : EvaluationManager.Maybe.of(null)),
      isNothing: () => value === null || value === undefined,
      getOrElse: (defaultValue) => (EvaluationManager.Maybe.of(value).isNothing() ? defaultValue : value),
    }),
  },
  /*------------------------------------*/
  matchError: (maybeEvaluation) => ({
    onNothing: (fn) => (maybeEvaluation.isNothing() ? fn() : EvaluationManager
      .matchError(maybeEvaluation)),
    onJust: (fn) => maybeEvaluation.isNothing() ? EvaluationManager
      .matchError(maybeEvaluation) : fn(maybeEvaluation.value),
  }),
  /*------------------------------------*/
  readQuestions: (file) => {
    const formFolderFiles = file
      .getParents()
      .next()
      .getFilesByName(`${file.getName()}.json`);
    if (formFolderFiles.hasNext()) {
      let { getBlob } = formFolderFiles.next();
      return JSON.parse(getBlob().getDataAsString());
    } else throw new Error(`Le fichier ${file.getName()}.json n'a pas été trouvé dans le même répertoire que le formulaire.`);
  },
  /*------------------------------------*/
  addQuestion: (form, question, propositions, index_correction, explication) => {
    const mci = form
      .addMultipleChoiceItem()
      .setTitle(question)
      .setPoints(1)
      .setRequired(true)
      .setFeedbackForIncorrect(
        FormApp.createFeedback()
          .setText(explication)
          .build());
    mci.setChoices(propositions.map((proposition, index) => {
      return mci.createChoice(proposition, index === index_correction);
    }));
  },
  /*------------------------------------*/
  loadForm: (form, questions) => {
    const { QUESTIONS_KEY, addQuestion, ERROR_QUESTIONS_MSG } = EvaluationManager;
    const { MULTIPLE_CHOICE } = FormApp.ItemType;
    try {

      PropertiesService.getScriptProperties().setProperty(
        QUESTIONS_KEY,
        JSON.stringify(questions));
      form.setProgressBar(true);

      const existingMcis = form
        .getItems(MULTIPLE_CHOICE)
        .map(({ getTitle }) => getTitle());

      questions.forEach((
        {
          question,
          propositions,
          index_correction,
          explication,
        }) => {
        if (!existingMcis.includes(question)) addQuestion(
          form,
          question,
          propositions,
          index_correction,
          explication);
      });

    } catch (error) {
      throw new Error(`${ERROR_QUESTIONS_MSG}${error.message}`);
    }
  },
  /*------------------------------------*/
  getQuestions: ({ getScriptProperties }) => {
    const json = getScriptProperties()
      .getProperty(EvaluationManager.QUESTIONS_KEY);
    return json ? JSON.parse(json).map(it => it) : null;
  },
  /*------------------------------------*/
  submitResults: (form, questions, { log }) => {
    const responses = FormApp.getActiveForm().getResponses();
    const responsesJson = responses.map((response) => response.getItemResponses());
    responsesJson.forEach(response => log(response));
    // TODO: Add processing

  },

  /*------------------------------------*/
  startEvaluation: () => {
    EvaluationManager
      .matchError(EvaluationManager.Maybe.of(EvaluationManager.readQuestions(EvaluationManager.file)))
      .onNothing(({ log }) => log(`L'évaluation n'a pas été correctement initialisée.`))
      .onJust(questions => EvaluationManager.loadForm(EvaluationManager.form, questions));
  },
  /*------------------------------------*/
  submitEvaluation: () => {
    EvaluationManager
      .matchError(EvaluationManager.Maybe.of(EvaluationManager.getQuestions(PropertiesService)))
      .onNothing(({ log }) => log(`L'évaluation n'a pas été correctement remplie.`))
      .onJust(questions => EvaluationManager.submitResults(EvaluationManager.form, questions, Logger));
  },
  /*------------------------------------*/
};

const FormTests = {
  /*------------------------------------*/
  logRunTests: ({ log }) => {
    log('Tests cases initiated.');
    Tests.logCanaryAssertions(log);
    log('/*-------------------------------------------------------*/');
    log('/*-------------- Canary Scenario -----------------------*/');
    Tests.canaryScenario(log);
    log('/*-------------- On StartUp Form Scenario --------------*/');
    FormTests.onStartupScenario(log);
    log('/*-------------- On Submit Form Scenario ---------------*/');
    FormTests.onSubmitScenario(log);
    log('/*-------------------------------------------------------*/');
    log('Tests cases completed.');
  },
  /*------------------------------------*/
  displayQuestions: (questions, log) => log(JSON.stringify(questions, null, '\t')),
  /*------------------------------------*/
  questions: EvaluationManager.readQuestions(EvaluationManager.file),
  /*------------------------------------*/
  // goodAnswers: [1, 1, 1, 1, 1, 0, 0, 2, 1, 2],
  goodAnswers: EvaluationManager.readQuestions(EvaluationManager.file).map(({ index_correction }) => index_correction),
  /*------------------------------------*/
  onStartupScenario: (log) => {
    const { assertTrue, assertNotNull, assertEquals } = Tests.Assertion;
    const { form, file, readQuestions, getQuestions } = EvaluationManager;
    const { questions } = FormTests;
    const mciQuestions = form
      .getItems()
      .map((mci) => {
        return {
          question: mci.getTitle(),
          propositions: mci
            .asMultipleChoiceItem()
            .getChoices()
            .map(choice => choice.getValue()),
        };
      });

    Tests.TestScenario
      .Feature('Training Evaluation.', log, () => {
      })
      .Scenario('On StartUp Form', log, () => {
      })
      .Given(`A FORM_ID and a json file, it gives two files.
      Both files have the same name and live in the same directory. 
      The json file contains questions,`, log, () => {
        assertEquals(
          file.getName(),
          file.getParents()
            .next()
            .getFilesByName(`${file.getName()}.json`)
            .next()
            .getName()
            .replace(/\.[^/.]+$/, ''));
        assertNotNull(JSON.stringify(questions, null, '\t'));
        assertTrue(Array.isArray(questions) && questions.length > 0);
        questions.forEach((question) => {
          assertTrue(Array.isArray(question['propositions']) && question['propositions'].length > 0);
          assertTrue(typeof question['index_correction'] === 'number' && !isNaN(question['index_correction']));
        });
      })
      .When('The onStartup function is called', () => {
        onStartup();
        log('\tscript read questions and load them in the form');
      })
      .Then('The questions are loaded ready for evaluation!', () => {
        getQuestions(PropertiesService)
          .forEach((question, index) => {
            assertEquals(
              questions[index]['question'],
              question['question']);
            assertEquals(
              mciQuestions[index]['question'],
              question['question']);
            assertEquals(
              questions[index]['index_correction'],
              question['index_correction']);
            assertEquals(
              questions[index]['explication'],
              question['explication']);
            question['propositions'].forEach((proposition, index_) => {
              assertEquals(
                questions[index]['propositions'][index_],
                proposition);
              assertEquals(
                mciQuestions[index]['propositions'][index_],
                proposition);
            });
          });
        log('\tuser can proceed his answers');
      });
  },
  /*------------------------------------*/
  onSubmitScenario:
    (log) => {
      const { assertTrue, assertEquals } = Tests.Assertion;
      const { form, file, readQuestions, getQuestions } = EvaluationManager;
      const { goodAnswers, questions } = FormTests;
      const responses = FormApp.getActiveForm().getResponses();

      Tests.TestScenario
        .Feature('Training Evaluation.', log, () => {
          readQuestions(file).forEach((question, index) => {
            assertEquals(
              question['explication'],
              getQuestions(PropertiesService)[index]['explication']);
          });
        })
        .Scenario('On Submit Form', log, () => {
          assertTrue(FormApp.getActiveForm().isQuiz());
          assertTrue(form.isQuiz());
        })
        .Given(`A completed form with good answers.`, log, () => {
          // log(`goodAnswers : [${goodAnswers}]`);
          const activeForm = FormApp.getActiveForm();
          const propertiesServiceQuestions = getQuestions(PropertiesService);
          assertEquals(propertiesServiceQuestions.length, questions.length);
          // log(`\tboucle autour des mcis de la form active`);
          // activeForm.getItems()
          //   .forEach((mci) => {
          //     log(`\t\t${mci.getTitle()}`);
          //   });
        })
        .When('The onSubmit function is called', () => {
          // onSubmit();
          // log('Result ot the quizz is sent the participant.');

        })
        .Then('The result of the quizz is sent to the participant!', () => {
        });
    },
  /*------------------------------------*/
};


const Tests = {
  /*------------------------------------*/
  TestScenario: {
    Feature: (feature, log, featureFunction) => ({
      Scenario: (scenario, log, scenarioFunction) => ({
        Given: (context, log, setupFunction) => ({
          When: (action, actionFunction) => ({
            Then: (assertion, assertionFunction) => {
              log(`Feature: ${feature}`);
              featureFunction();
              log(`Scenario: ${scenario}`);
              scenarioFunction();
              log(`Given: ${context}`);
              setupFunction();
              log(`When: ${action}`);
              actionFunction();
              log(`Then: ${assertion}`);
              assertionFunction();
            },
          }),
        }),
      }),
    }),
  },
  /*------------------------------------*/
  Assertion: {
    assertEquals: (actual, expected, message = '') => {
      if (actual !== expected) throw new Error(`Assertion Failed: ${message}. Expected ${expected}, but got ${actual}`);
    },
    assertTrue: (condition, message = '') => {
      if (!condition) throw new Error(`Assertion Failed: ${message}. Expected true, but got false`);
    },
    assertFalse: (condition, message = '') => {
      if (condition) throw new Error(`Assertion Failed: ${message}. Expected false, but got true`);
    },
    assertUndefined: (value, message = '') => {
      if (typeof value !== 'undefined') throw new Error(`Assertion Failed: ${message}. Expected undefined, but got ${value}`);
    },
    assertNotNull: (value, message = '') => {
      if (value === null) throw new Error(`Assertion Failed: ${message}. Expected not null, but got null`);
    },
    assertNotNaN: (value, message = '') => {
      if (isNaN(value)) throw new Error(`Assertion Failed: ${message}. Expected not NaN, but got NaN`);
    },
    expectedException: (func, expectedError, message = '') => {
      try {
        func();
        throw new Error(`Assertion Failed: ${message}. Expected an exception of type ${expectedError}`);
      } catch (error) {
        if (!(error instanceof expectedError)) throw new Error(`Assertion Failed: ${message}. Expected an exception of type ${expectedError}, but got ${error}`);
      }
    },
  },
  /*------------------------------------*/
  ERROR_TESTS_MSG: `Erreur lors du chargement des tests : `,
  CANARY_MSG: `Something unexpected happened in the canary`,
  /*------------------------------------*/
  logCanaryAssertions: (log) => {
    const {
      assertUndefined,
      assertTrue,
      assertFalse,
      assertNotNaN,
      assertNotNull,
      expectedException,
      assertEquals,
    } = Tests.Assertion;
    assertEquals(2 + 2, 4, 'Adding numbers');
    assertTrue(2 + 2 === 4, 'Adding numbers');
    assertFalse(2 + 2 === 5, 'Adding numbers');
    const someFunction = () => {
      throw new TypeError('This is a test error');
    };
    expectedException(someFunction, TypeError, 'Testing expected exception');
    let undefinedVariable;
    assertUndefined(undefinedVariable, 'Undefined variable check');
    assertNotNull(log, 'Null value check');
    assertNotNaN('123', 'NaN check');
    try {
      throw new Error(`${Tests.ERROR_TESTS_MSG}canary`);
    } catch (error) {
      if (error.message !== `${Tests.ERROR_TESTS_MSG}canary`) throw new Error(`${Tests.ERROR_TESTS_MSG}${Tests.CANARY_MSG}`);
    }
    expectedException(Tests.logCanaryAssertions, Error, 'Testing canary expected exception');
  },
  /*------------------------------------*/
  canaryScenario: (log) => {
    Tests.TestScenario
      .Feature('Training Evaluation.', log, () => {
      })
      .Scenario('Canary Scenario', log, () => {
      })
      .Given('A user is logged in', log, () => {
        // Setup function to log in a user
        // log('user is logged in');
      })
      .When('The user clicks on the logout button', () => {
        // Action function to simulate clicking on the logout button
        // log('user clicks on the logout button');
      })
      .Then('The user should be logged out', () => {
        // Assertion function to check if the user is logged out
        // log('user is logged out');
      });
  },
  /*------------------------------------*/
};
