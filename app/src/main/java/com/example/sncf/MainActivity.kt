package com.example.sncf

import com.example.sncf.classes.Station
import com.example.sncf.classes.Train
import com.example.sncf.api.SNCFapi

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView


import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONException
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val sncfAPI: SNCFapi = SNCFapi(
        "https://api.sncf.com/v1",
        "token"
    )


    private lateinit var selectedStation: Station

    private var trains: List<Train> = arrayListOf()

    private var trainsViewAdapter: ArrayAdapter<Train>? = null
    private lateinit var trainsListView: ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val stations = initListStations()

        val input = findViewById<AutoCompleteTextView>(R.id.saisie_gare)

        trainsListView = findViewById(R.id.list_train)
        trainsViewAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, trains)
        trainsListView.adapter = trainsViewAdapter

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stations)
        input.setAdapter(adapter)


        input.setOnItemClickListener { parent, _, position, _ ->
            this.selectedStation = parent.getItemAtPosition(position) as Station
            trainsViewAdapter?.clear()

            sncfAPI.getRequest1(selectedStation.uic, callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    this@MainActivity.runOnUiThread {

                        val trains = sncfAPI.parseDepartures(response.body as ResponseBody, selectedStation)

                        trainsViewAdapter!!.addAll(trains)

                        trains.forEach { train ->
                            sncfAPI.getRequest2(train.numJourney, callback = object : Callback {
                                override fun onFailure(call: Call, e: IOException)
                                {
                                    e.printStackTrace()
                                }

                                override fun onResponse(call: Call, response: Response)
                                {
                                    try {
                                        sncfAPI.parseVehicleJourneys(train, response.body!!)
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                }
                            })
                        }

                        trainsViewAdapter?.notifyDataSetChanged()
                        trainsListView.adapter = trainsViewAdapter

                        hideKeyboardOnChange()
                    }
                }
            })
        }


        trainsListView.setOnItemClickListener { parent, _, position, _ ->
            val train = parent.getItemAtPosition(position) as Train
            val intent = Intent(this, MapsActivity::class.java)

            intent.putExtra("train", train)

            // Start MapsActivity
            startActivity(intent)
        }
    }




    private fun initListStations(): List<Station> {

        val inputStream = resources.openRawResource(R.raw.gares)
        val reader = inputStream.bufferedReader()

        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {

                val (code, libelle, lg, lat) = it.split(';', ignoreCase = false, limit = 4)
                Station(code.toInt(), libelle, lg.toDouble(), lat.toDouble())

            }.toList()
    }


    private fun hideKeyboardOnChange() {
        this@MainActivity.currentFocus?.let { view ->
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}