/*=================================================================================*/
const FORM_ID = "18mThYQW1q-az-OpjrhXW8Ag5o2kb7n7QQoSzlKrm9p0";
const SPREADSHEET_URL = "https://docs.google.com/spreadsheets/d/1qv_4HN-MM6OhRG0W1m7RFJ8Ryu0Ob1DapjwSIiBk9k4/edit?usp=drive_link";
/*=================================================================================*/
const onStartUp = () => EvaluationManager
    .matchError(EvaluationManager.Maybe.of(EvaluationManager.loadEvaluation(FORM_ID, SPREADSHEET_URL)))
    .onNothing(({ log }) => log("L'évaluation n'a pas été correctement initialisée."))
    .onJust((evaluation) => {
        EvaluationManager.saveEvaluation(evaluation, PropertiesService);
        EvaluationManager.loadFormQuestions(evaluation);
        EvaluationManager.getQuestions(evaluation).forEach(q => Logger.log(q));
    });
/*=================================================================================*/
const onSubmit = () => EvaluationManager
    .matchError(EvaluationManager.Maybe.of(EvaluationManager.getEvaluation(PropertiesService)))
    .onNothing(({ log }) => log("L'évaluation n'a pas été correctement remplie."))
    .onJust((evaluation) => {
        Logger.log("Cycle de vie de la Form: A la soumission du QCM");
        EvaluationManager.processResponses(Logger, evaluation);
//    tests.logEvaluation(evaluation, Logger);
    });
/*=================================================================================*/
const check = () => tests.logRunTests(Logger);
/*=================================================================================*/
