package com.example.happyplaces.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener{
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage : Uri? = null
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0
    private var mHappyPlaceDetail : HappyPlaceModel? = null
    private var etDate : EditText? = null
    var etTitle : EditText? = null
    var etDescription : EditText? = null
    var etLocation : EditText? = null
    private var ivPlaceImage : ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)
        val toolbarAddPlace : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_add_place)
        setSupportActionBar(toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAIL)){
            mHappyPlaceDetail = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAIL) as HappyPlaceModel?
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        etDate  = findViewById(R.id.et_date)
        etTitle = findViewById(R.id.et_title)
        etDescription = findViewById(R.id.et_description)
        etLocation = findViewById(R.id.et_location)
        val tvAddImage : TextView = findViewById(R.id.tv_add_image)
        val btnSave : Button = findViewById(R.id.btn_save)
        updateDateInView()

        if(mHappyPlaceDetail != null){
            supportActionBar?.title = "Edit Happy Place"

            etTitle!!.setText(mHappyPlaceDetail!!.title)
            etDescription!!.setText(mHappyPlaceDetail!!.description)
            etDate!!.setText(mHappyPlaceDetail!!.date)
            etLocation!!.setText(mHappyPlaceDetail!!.location)
            mLatitude = mHappyPlaceDetail!!.latitude
            mLongitude = mHappyPlaceDetail!!.longitude

            saveImageToInternalStorage = Uri.parse(
                mHappyPlaceDetail!!.image
            )
            ivPlaceImage = findViewById(R.id.iv_place_image)
            ivPlaceImage!!.setImageURI(saveImageToInternalStorage)

            btnSave.text = "UPDATE"
        }
        etDate?.setOnClickListener(this)
        tvAddImage.setOnClickListener(this)
        btnSave.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date -> {
                DatePickerDialog(this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image -> {
                val pickerDialog = AlertDialog.Builder(this)
                pickerDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery", "Capture photo from camera")
                pickerDialog.setItems(pictureDialogItems){
                    dialog, which ->
                    when(which){
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pickerDialog.show()
            }
            R.id.btn_save -> {
                val etTitle : EditText = findViewById(R.id.et_title)
                val etDescription : EditText = findViewById(R.id.et_description)
                val etLocation : EditText = findViewById(R.id.et_location)
                when{
                    etTitle.text.isNullOrEmpty() ->{
                        Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                    }
                    etDescription.text.isNullOrEmpty() ->{
                        Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                    }
                    etLocation.text.isNullOrEmpty() ->{
                        Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                    }else -> {
                        val happyPlaceModel = HappyPlaceModel(
                            if (mHappyPlaceDetail == null) 0 else mHappyPlaceDetail!!.id,
                            etTitle.text.toString(),
                            saveImageToInternalStorage.toString(),
                            etDescription.text.toString(),
                            etDate?.text.toString(),
                            etLocation.text.toString(),
                            mLatitude,
                            mLongitude,
                        )
                    val dbHandler = DatabaseHandler(this)
                    if(mHappyPlaceDetail == null) {
                        val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                    if (addHappyPlace > 0 ){
//                        Toast.makeText(
//                            this,
//                            "The happy place details are inserted successfully!",
//                            Toast.LENGTH_SHORT
//                        ).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    }else{
                        val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                        if (updateHappyPlace > 0 ){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                    }
                }
            }
        }
    }
    private fun takePhotoFromCamera() {

        Dexter.withActivity(this)
            .withPermissions(

                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    // Here after all the permission are granted launch the CAMERA to capture an image.
                    if (report!!.areAllPermissionsGranted()) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread()
            .check()
    }

    private fun choosePhotoFromGallery(){
        Dexter.withActivity(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
//                    Toast.makeText(
//                        this@AddHappyPlaceActivity,
//                        "Storage READ/WRITE permission are granted. Now you can select image from GALLERY",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest> , token: PermissionToken) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
         ivPlaceImage = findViewById(R.id.iv_place_image)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        // Here this is used to get an bitmap from URI
                        @Suppress("DEPRECATION")

                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image: ", "Path:: $saveImageToInternalStorage")
                        ivPlaceImage!!.setImageBitmap(selectedImageBitmap) // Set the selected image from GALLERY to imageView.
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity, "Failed!", Toast.LENGTH_SHORT).show()
                    }
                }

            } else if (requestCode == CAMERA) {

                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap // Bitmap from camera

                 saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image: ", "Path:: $saveImageToInternalStorage")

                ivPlaceImage!!.setImageBitmap(thumbnail) // Set to the imageView.
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "Cancelled")
        }
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage(""+
                "It looks like you have turned off permission required " +
                "for this feature." +
                " It can be enabled under the Application Settings")
            .setPositiveButton("GO TO SETTINGS"){
                _,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",packageName,null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){
                dialog, which ->
                dialog.dismiss()

            }.show()
    }

    private fun updateDateInView(){
        val  etDate : EditText = findViewById(R.id.et_date)
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        etDate.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) : Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY,Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }
}