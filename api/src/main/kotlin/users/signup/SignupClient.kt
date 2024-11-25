package users.signup

import org.springframework.web.service.annotation.HttpExchange
import users.dao.UserDao

@HttpExchange(UserDao.UserRestApiRoutes.API_USERS)
interface SignupClient {
}