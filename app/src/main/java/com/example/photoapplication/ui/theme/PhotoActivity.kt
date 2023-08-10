package com.example.photoapplication.ui.theme

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.photoapplication.ui.theme.ui.theme.PhotoApplicationTheme
import com.example.photoapplication.ui.theme.viewmodel.PhotoViewModel


class PhotoActivity : ComponentActivity() {
    val photoViewModel: PhotoViewModel by viewModels()
    var imageFileUriList = ArrayList<Uri>()
    var filteredImageUris = ArrayList<Uri>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhotoApplicationTheme {
                // A surface container using the 'background' color from the theme

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val listNumber by photoViewModel.itemNumber.collectAsState()
                    val imageUris by photoViewModel.imageUris.collectAsState()

                    var number by remember { mutableStateOf("0") }



                        if (listNumber == 0) {

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(40.dp)
                            ) {

                                Text(
                                    "Photo, please enter desired number between 10 and 50 of pictures",
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(top = 80.dp)
                                )
                                TextField(
                                    value = "$number",
                                    onValueChange = {
                                        number = it
                                    },
                                    modifier = Modifier
                                        .padding(top = 80.dp)
                                        .align(Alignment.CenterHorizontally),
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                                )
                                Button(
                                    modifier = Modifier
                                        .padding(top = 80.dp)
                                        .align(Alignment.CenterHorizontally), onClick = {
                                        if (number.toInt() in 11..49) {
                                            photoViewModel.updateItemNumber(number.toInt())
                                            if (checkPermission()) openGallery() else requestPermission()


                                        } else {
                                            Toast.makeText(
                                                this@PhotoActivity,
                                                "Please enter valid number",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }) {
                                    Text(text = "Done")
                                }
                            }


                        }
                        imageUris.forEach {
                            Log.i("Number imageUris", "$it")
                        }
                        if (imageUris.size > 0)
                            LazyColumn(
                                modifier = Modifier
                                    .wrapContentSize()
                            ) {
                                items(number.toInt()) { index ->
                                    if (isIndexTriangular(index)) {
                                        Log.i("Number yes $index", "${imageUris[0]}")
                                        Image(
                                            modifier = Modifier.fillMaxWidth().height(90.dp),
                                            contentScale = ContentScale.Crop,
                                            painter = rememberImagePainter(data = imageUris[0]),
                                            contentDescription = null
                                        )
                                    } else {
                                        Log.i("Number no $index", "${imageUris[1]}")

                                        Image(
                                            modifier = Modifier.fillMaxWidth().height(90.dp),
                                            contentScale = ContentScale.Crop,
                                            painter = rememberImagePainter(data = imageUris[1]),
                                            contentDescription = null
                                        )
                                    }
                                }
                            }


                }
            }
        }
    }

    private fun isIndexTriangular(number: Int): Boolean {
        var sum = 0
        var n = 1

        while (sum < number) {
            sum += n
            n++
        }

        return sum == number
    }

    private fun openGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        galleryIntent.type = "image/*"
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
    }

    private fun checkPermission(): Boolean {
        var permissionGranted = ContextCompat.checkSelfPermission(
            this,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED



        return permissionGranted
    }

    private val RESULT_LOAD_IMAGE = 1

    private fun requestPermission() {
        requestPermissions(
            arrayOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), RESULT_LOAD_IMAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_LOAD_IMAGE -> if (null != data && resultCode == Activity.RESULT_OK) {

                    if (data.clipData == null) {
                        data.data?.let {
                            imageFileUriList.add(it)
                        }
                    } else {
                        data.clipData?.let { clipData ->
                            for (i in 0 until clipData.itemCount) {
                                clipData.getItemAt(i).uri.let { uri ->
                                    imageFileUriList.add(uri)
                                }
                            }
                        }

                    }

                    photoViewModel.populateImageUris(imageFileUriList)


                }
            }
        } else {
            photoViewModel.updateItemNumber(0)
            Toast.makeText(this, "Images not selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RESULT_LOAD_IMAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {

                    openGallery()
                }
                return
            }

        }
    }
}

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    PhotoApplicationTheme {
        Greeting2("Android")
    }
}