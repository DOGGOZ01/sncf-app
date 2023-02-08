package com.example.sncf.api

import com.example.sncf.classes.Station
import com.example.sncf.classes.Train
import com.example.sncf.classes.Stop
import com.example.sncf.classes.TypeTrain

import okhttp3.*
import org.json.JSONException
import org.json.JSONObject

class SNCFapi(
    private var url: String,
    private var apiToken: String ){

    private var client: OkHttpClient = OkHttpClient()


    fun getRequest1(codeUIC: Int, callback: Callback) {
        val url = url + "/coverage/sncf/stop_areas/stop_area:SNCF:" + codeUIC + "/departures?key=" + apiToken + "&count=8"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(callback)
    }

    fun getRequest2(vehicleJourneyId: String, callback: Callback) {
        val url = url + "/coverage/sncf/vehicle_journeys/" + vehicleJourneyId + "/vehicle_journeys?key=" + apiToken
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(callback)
    }


    fun parseDepartures(json: ResponseBody, station: Station): List<Train> {
        val tmpTrains = mutableListOf<Train>()
        try {
            val obj = JSONObject(json.string())
            val departures = obj.getJSONArray("departures")

            for (i in 0 until departures.length()) {

                val trainObj = departures.getJSONObject(i)
                val trainInfo = trainObj.getJSONObject("display_informations")

                val trainStopDateTime = trainObj.getJSONObject("stop_date_time")
                val links = trainObj.getJSONArray("links")
                val libelle = trainInfo.getString("direction")
                val num = trainInfo.getString("trip_short_name")

                val type = trainInfo.getString("commercial_mode").split(" ")[0]
                val timestamp = trainStopDateTime.getString("departure_date_time")

                val (heure, minute) = timestamp.split("T").last().chunked(2)
                val vehicleJourney = links[1] as JSONObject

                val vehicleJourneyId = vehicleJourney.getString("id")

                val stopFrom = Stop(heure, minute, heure, minute,station)

                val typeTrain =
                    try {
                    TypeTrain.valueOf(type)
                }
                catch (e: java.lang.IllegalArgumentException) {
                    TypeTrain.TER
                }

                val train = Train(num, libelle, typeTrain, heure, minute, vehicleJourneyId).apply { from = stopFrom }
                tmpTrains.add(train)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return tmpTrains
    }

    fun parseVehicleJourneys(train: Train, json: ResponseBody): Train {
        val obj = JSONObject(json.string())
        val vehicleJourney = obj.getJSONArray("vehicle_journeys").getJSONObject(0)
        val stopTimes = vehicleJourney.getJSONArray("stop_times")

        for (i in 0 until stopTimes.length()) {
            val stopTime = stopTimes.getJSONObject(i)

            val arrivalH = stopTime.getString("arrival_time").substring(0, 2)
            val arrivalM = stopTime.getString("arrival_time").substring(2, 4)

            val departureH = stopTime.getString("departure_time").substring(0, 2)
            val departureM = stopTime.getString("departure_time").substring(2, 4)

            val stopPoint = stopTime.getJSONObject("stop_point")
            val uicCode = stopPoint.getString("id").split(':', ignoreCase = false, limit = 4)[2].toInt()
            val name = stopPoint.getString("name")
            val lat = stopPoint.getJSONObject("coord").getDouble("lat")
            val lon = stopPoint.getJSONObject("coord").getDouble("lon")

            val station = Station(uicCode, name, lon, lat)
            val stop = Stop(arrivalH, arrivalM, departureH, departureM, station)
            stop.setStation(station)

            train.addStop(stop, !stopTime.getBoolean("drop_off_allowed"), !stopTime.getBoolean("pickup_allowed"))
        }

        return train
    }
}



