package com.example.uinavegacion.data.remote.jsonplaceholder

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Servicio Retrofit liviano para consumir los endpoints públicos de JSONPlaceholder.
 * Se usa como referencia de cómo conectar la app con microservicios reales mediante rutas REST.
 */
interface JsonPlaceholderService {

    @GET("posts")
    suspend fun getPosts(): List<PostDto>

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): PostDto

    @GET("posts/{id}/comments")
    suspend fun getPostComments(@Path("id") id: Int): List<CommentDto>

    @GET("comments")
    suspend fun getCommentsByPost(@Query("postId") postId: Int): List<CommentDto>

    @POST("posts")
    suspend fun createPost(@Body request: PostPayload): PostDto

    @PUT("posts/{id}")
    suspend fun replacePost(
        @Path("id") id: Int,
        @Body request: PostPayload
    ): PostDto

    @PATCH("posts/{id}")
    suspend fun updatePostPartial(
        @Path("id") id: Int,
        @Body request: Map<String, Any?>
    ): PostDto

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Int)
}

data class PostDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

data class CommentDto(
    val postId: Int,
    val id: Int,
    val name: String,
    val email: String,
    val body: String
)

data class PostPayload(
    val userId: Int,
    val title: String,
    val body: String
)



