package com.griffith.maptrackerproject.DB

import java.util.Date

class MockLocationsDAO : LocationsDAO {
    // Mock data
    private val mockLocations = listOf(
        Locations( 52.5200, 13.4050,0.0, Date(123, 0, 1)),
        Locations(52.5201, 13.4051,0.0, Date(123, 0, 2)),
    )

    override suspend fun insert(vararg location: Locations) {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationsForDay(date: String): List<Locations> {
        return mockLocations
    }

    override suspend fun getAllLocations(): List<Locations> {
        return mockLocations
    }

}