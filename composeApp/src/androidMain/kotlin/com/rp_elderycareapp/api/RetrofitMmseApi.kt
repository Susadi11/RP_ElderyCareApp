package com.rp_elderycareapp.api

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

interface RetrofitMmseApi {
    @Multipart
    @POST("mmse/submit")
    suspend fun submitMmse(
        @Part("assessment_id") assessmentId: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("question_type") questionType: RequestBody,
        @Part("caregiver_is_correct") caregiverIsCorrect: RequestBody?,
        @Part file: MultipartBody.Part
    ): retrofit2.Response<Unit>
}

fun File.toMultipart(fieldName: String = "file"): MultipartBody.Part {
    val requestFile = this.asRequestBody("audio/wav".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(fieldName, this.name, requestFile)
}

fun String.toRequestBody(): RequestBody {
    return RequestBody.create("text/plain".toMediaTypeOrNull(), this)
}
