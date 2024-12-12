const ERROR_QUESTIONS_MSG = "Erreur lors du chargement des questions du formulaire : ";
const ERROR_EVALUATION_MSG = "Erreur lors du chargement de l'évaluation : ";


const EvaluationManager = {

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

  getQuestions: (evaluation) => evaluation
      .sheetData
      .slice(1)
      .map((exercice) => exercice[0]),

  /*=================================================================================*/

  getExistingTitles: ({ getItems }) => new Set(getItems().map(({ getTitle }) => getTitle())),

  /*=================================================================================*/

  loadFormQuestions: (evaluation) => {
    try {
      const existingTitles = EvaluationManager.getExistingTitles(evaluation.form);
      evaluation
          .sheetData
          .slice(1)
          .map(([question, propositions]) => ({
            question,
            propositions: Array
                .from(JSON.parse(`[${propositions}]`))
                .map(prop => prop.trim()),
          }))
          .forEach(({ question, propositions }) => {
            if (!existingTitles.has(question)) {
              EvaluationManager.addQuestionToForm(
                  evaluation.form,
                  question,
                  propositions);
              existingTitles.add(question);
            }
          });
    } catch (error) {
      throw new Error(`${ERROR_QUESTIONS_MSG}${error.message}`);
    }
  },

  /*=================================================================================*/
  loadEvaluation: (id, url, { openById } = SpreadsheetApp, { openById: openFormById } = FormApp) => {
    try {
      const sheetId = EvaluationManager.getSheetIdFromUrl(url);
      return {
        form: openFormById(id),
        sheetId,
        sheetData: openById(sheetId)
            .getActiveSheet()
            .getDataRange()
            .getValues(),
      };
    } catch (error) {
      throw new Error(`${ERROR_EVALUATION_MSG}${error.message}`);
    }
  },

  /*=================================================================================*/
  getEvaluation: ({ getScriptProperties }) => {
    const evaluationString = getScriptProperties()
        .getProperty("evaluation");
    return evaluationString ? JSON.parse(evaluationString) : null;
  },

  /*=================================================================================*/
  saveEvaluation: (evaluation, { getScriptProperties }) => {
    getScriptProperties()
        .setProperty("evaluation", JSON.stringify(evaluation));
    return evaluation;
  },

  /*=================================================================================*/
  addQuestionToForm: ({ addMultipleChoiceItem }, question, propositions) => {
    addMultipleChoiceItem()
        .setTitle(question)
        .setChoiceValues(propositions)
        .setPoints(1)
        .setRequired(true);
  },

  /*=================================================================================*/
  getSheetIdFromUrl: (url) => {
    const matches = url.match(/\/d\/([a-zA-Z0-9-_]+)/);
    return matches && matches[1] ? matches[1] : null;
  },
  /*=================================================================================*/
  processResponses: ({ log }, evaluation) => {
    log("processResponses");
  }
};
/**
 {sheetId=1qv_4HN-MM6OhRG0W1m7RFJ8Ryu0Ob1DapjwSIiBk9k4, sheetData=[[Question, Propositions, Correction, Explication], [Qu'est-ce que Google Form Script ?, "Un service de traitement de texte en ligne", "Un langage de programmation pour automatiser des actions dans Google Forms", "Un outil de conception graphique pour formulaires", 1.0, Google Form Script est un langage de programmation permettant d'automatiser des actions dans Google Forms à l'aide de scripts associés.]]], form={}}
 */
