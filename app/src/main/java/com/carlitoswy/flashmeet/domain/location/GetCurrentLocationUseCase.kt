package com.carlitoswy.flashmeet.domain.location

import android.location.Location
import com.carlitoswy.flashmeet.data.location.LocationService
import kotlinx.coroutines.flow.Flow

class GetCurrentLocationUseCase(
    private val locationService: LocationService
) {
    operator fun invoke(): Flow<Location> = locationService.getLocationUpdates()
}
