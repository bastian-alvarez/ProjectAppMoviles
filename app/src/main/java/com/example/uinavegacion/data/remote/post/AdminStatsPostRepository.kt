package com.example.uinavegacion.data.remote.post

/**
 * Remote contract for aggregated dashboard statistics exposed by the
 * administration analytics microservice.
 */
class AdminStatsPostRepository : BasePostRepository(serviceName = "admin-stats") {

    suspend fun fetchDashboardStats(): Result<DashboardStatsRemoteDto> {
        return notImplemented("fetchDashboardStats")
    }

    suspend fun refreshDashboardCache(): Result<Unit> {
        return notImplemented("refreshDashboardCache")
    }
}

