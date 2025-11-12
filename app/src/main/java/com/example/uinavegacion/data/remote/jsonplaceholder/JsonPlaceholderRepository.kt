package com.example.uinavegacion.data.remote.jsonplaceholder

/**
 * Repositorio minimalista que encapsula las llamadas al servicio de ejemplo.
 * La idea es que puedas copiar esta estructura y reemplazar la URL base + rutas
 * por las de tus microservicios.
 */
class JsonPlaceholderRepository(
    private val service: JsonPlaceholderService = JsonPlaceholderApi.service
) {

    suspend fun fetchPosts(): List<PostDto> = service.getPosts()

    suspend fun fetchPostDetail(id: Int): PostDto = service.getPost(id)

    suspend fun fetchCommentsForPost(id: Int): List<CommentDto> = service.getPostComments(id)

    suspend fun createPost(request: PostPayload): PostDto = service.createPost(request)

    suspend fun replacePost(id: Int, request: PostPayload): PostDto =
        service.replacePost(id, request)

    suspend fun updatePostTitle(id: Int, newTitle: String): PostDto =
        service.updatePostPartial(id, mapOf("title" to newTitle))

    suspend fun deletePost(id: Int) {
        service.deletePost(id)
    }
}


