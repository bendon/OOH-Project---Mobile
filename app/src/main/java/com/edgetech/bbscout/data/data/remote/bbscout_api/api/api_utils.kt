package com.edgetech.bbscout.data.data.remote.bbscout_api.api


import com.edgetech.bbscout.components.utils.log
import com.edgetech.bbscout.components.utils.toLong
import com.edgetech.bbscout.data.data.local.BBScoutDao
import com.edgetech.bbscout.data.data.local.utils.BBScoutDatabase
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AccountResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.ApiResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.BadRequestException
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.IoErrorException
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.NetworkErrorException
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.NotFoundException
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.ServerErrorException
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.UnAuthenticatedException
import com.edgetech.bbscout.data.utils.BBScoutException
import com.edgetech.bbscout.data.utils.SimpleResource
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject

class RepositoryHelper @Inject constructor(
    val arSpringApi: BBScoutApi,
    val database: BBScoutDatabase
) {

    suspend fun <T : ApiResponse> apiDbRequestOrFail(
        numberCall: Int = 0,
        call: suspend () -> Response<T>,
    ): SimpleResource<T> {
        return try {
            val response = call.invoke()
            if (response.isSuccessful) {
                return SimpleResource.Success(response.body()!!)
            } else {
                val invalidToken =
                    checkIfErrorIsInvalidToken(arSpringApi, database.bbScoutDao, response.code())
                if (invalidToken && numberCall < 2) {
                    return apiDbRequestOrFail(numberCall + 1, call)
                }
                return onHttpApiError(response)
            }
        } catch (e: HttpException) {
            log("An Http error occurred $e")
            SimpleResource.Error(e.message(),)
        } catch (e: IOException) {
            log("An IO error occurred $e")
            SimpleResource.Error(
                e.message ?: "Couldn't reach server, check your internet connection",
                IoErrorException()
            )
        } catch (e: NetworkErrorException) {
            log("An Network error occurred $e")
            SimpleResource.Error(
                e.message ?: "Couldn't reach server, check your internet connection",
                NetworkErrorException()
            )
        } catch (e: Exception) {
            log("An Unknown error occurred $e")
            //log("An Unknown error occurred $e with request ${e.stackTrace}")
            SimpleResource.Error(e.message ?: "An Unknown error occurred", BBScoutException())
        }

    }

}

 fun <T : ApiResponse> onHttpApiError(response: Response<T>): SimpleResource.Error<T> {
    val error = response.body()
    if (response.code() == 400) {
        return SimpleResource.Error(
            response.message(),
            BadRequestException()
        )
    } else if (response.code() == 404) {
        return SimpleResource.Error(
            response.message(),
            NotFoundException()
        )
    } else if (response.code() == 401) {
        return SimpleResource.Error(
            response.message(),
            UnAuthenticatedException()
        )
    } else if (response.code() in 500..599) {
        return SimpleResource.Error(
            response.message(),
            ServerErrorException()
        )
    }


    return SimpleResource.Error(
        response.message(),
        BBScoutException()
    )
}

suspend fun checkIfErrorIsInvalidToken(api: BBScoutApi, dao: BBScoutDao, error: Int): Boolean {
    if (error == 401) {
        try {
            val authObject = dao.getAuth()
            val response = api.refreshToken("Bearer ${authObject.refreshToken}")
            if (response.isSuccessful) {
                val auth = response.body()
                if (auth != null) {
                    val newAuth = authObject.copy(
                        accessToken = auth.accessToken,
                        refreshToken = auth.refreshToken,
                        timeAdded = LocalDateTime.now().toLong()
                    )
                    dao.insertAuthResponse(newAuth)
                    return true
                } else {
                    return false
                }
            } else {
                return false
            }
        } catch (e: Exception) {
            return false
        }

    } else {
        return false
    }
}


