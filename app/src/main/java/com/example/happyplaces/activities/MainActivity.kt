package com.example.happyplaces.activities

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private var rvHappyPlaceList : RecyclerView? = null
    private var tvNoRecordAvailable : TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fabAddHappyPlace : FloatingActionButton = findViewById(R.id.fabAddHappyPlace)
        fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }
        getHappyPlacesListFromLocalDB()
    }
    private fun setupHappyPlacesRecycleView(happyPlaceList: ArrayList<HappyPlaceModel>){
         rvHappyPlaceList = findViewById(R.id.rv_happy_places_list)
        rvHappyPlaceList?.layoutManager = LinearLayoutManager(this)

        rvHappyPlaceList?.setHasFixedSize(true)
        val placesAdapter = HappyPlacesAdapter(this,happyPlaceList)
        rvHappyPlaceList?.adapter = placesAdapter
    }
    private fun getHappyPlacesListFromLocalDB(){
        tvNoRecordAvailable = findViewById(R.id.tv_no_record_available)
        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList : ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()
        if(getHappyPlaceList.size >0){
            for (i in getHappyPlaceList){
                rvHappyPlaceList?.visibility = View.VISIBLE
                tvNoRecordAvailable?.visibility = View.GONE
                setupHappyPlacesRecycleView(getHappyPlaceList)
            }
        }else{
            rvHappyPlaceList?.visibility = View.GONE
            tvNoRecordAvailable?.visibility = View.VISIBLE
        }
    }
}