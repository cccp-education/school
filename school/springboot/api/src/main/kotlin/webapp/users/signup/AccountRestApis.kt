package webapp.users.signup

/** Account REST API URIs */
object AccountRestApis {
  const val API_AUTHORITY = "/api/authorities"
  const val API_ACCOUNT = "/api/accounts"
  const val API_SIGNUP = "/signup"
  const val API_SIGNUP_PATH = "$API_ACCOUNT$API_SIGNUP"
  const val API_ACTIVATE = "/activate"
  const val API_ACTIVATE_PATH = "$API_ACCOUNT$API_ACTIVATE?key="
  const val API_ACTIVATE_PARAM = "{activationKey}"
  const val API_ACTIVATE_KEY = "key"
  const val API_RESET_INIT = "/reset-password/init"
  const val API_RESET_FINISH = "/reset-password/finish"
  const val API_CHANGE = "/change-password"
  const val API_CHANGE_PATH = "$API_ACCOUNT$API_CHANGE"
}