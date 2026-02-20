package com.codelabs.state

import android.app.Application
import com.codelabs.state.data.WellnessDatabase

class WellnessApplication : Application() {
    val database: WellnessDatabase by lazy { WellnessDatabase.getDatabase(this) }
}
