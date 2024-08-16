package com.app.tplsamplecompose

import android.app.appsearch.SearchResult
import android.app.appsearch.SearchResults
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.app.tplsamplecompose.ui.theme.TPLSampleComposeTheme
import com.app.tplsamplecompose.ui.theme.iconColor
import com.tplmaps.sdk.places.LngLat
import com.tplmaps.sdk.places.OnSearchResult
import com.tplmaps.sdk.places.Params
import com.tplmaps.sdk.places.Place
import com.tplmaps.sdk.places.SearchHelper
import com.tplmaps.sdk.places.SearchManager
import com.tplmaps3d.CameraPosition
import com.tplmaps3d.MapController
import com.tplmaps3d.MapView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TPLSampleComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapViewContainer()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewContainer() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current



    // States to hold MapController and MapView
    var mapController by remember { mutableStateOf<MapController?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var address by remember { mutableStateOf("")}
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    var isSearch by remember { mutableStateOf(false) }

    var searchManager by remember { mutableStateOf<SearchManager?>(null) }
    var searchLngLat by remember { mutableStateOf<LngLat?>(null) }
    var searchResultListener by remember { mutableStateOf<OnSearchResult?>(null) }
    var searchResults by remember { mutableStateOf<List<Place>>(emptyList()) }



    Box(modifier = Modifier.fillMaxSize()) {

        // AndroidView to embed MapView
        AndroidView(
            factory = {
                val mapViewInstance = MapView(context)
                searchManager = SearchManager(context as ComponentActivity)


                // Define a custom OnSearchResult listener
                 searchResultListener = object : OnSearchResult {
                    override fun onSearchResult(results: ArrayList<com.tplmaps.sdk.places.Place>) {
                        // Handle search results here
                        // For example, update the address state with the first result's details if needed
                        if (results.isNotEmpty()) {
                            if (isSearch) {
                                searchResults = results
                                Log.d("TAG", "onSearchResult: $results")
                                isSearch = false
                            }
                            else {
                                val place = results.first()
                                address = place.name
                            }
                        }
                    }

                    override fun onSearchResultNotFound(params: Params, requestTimeInMS: Long) {
                        Log.d("TAG", "onSearchResult: 2")

                        // Handle no results
                    }

                    override fun onSearchRequestFailure(e: Exception) {
                        // Handle request failure
                        Log.d("TAG", "onSearchResult: 3")

                    }

                    override fun onSearchRequestCancel(params: Params, requestTimeInMS: Long) {
                        // Handle request cancel
                        Log.d("TAG", "onSearchResult: 4")

                    }

                    override fun onSearchRequestSuspended(errorMessage: String, params: Params, requestTimeInMS: Long) {
                        // Handle request suspension
                        Log.d("TAG", "onSearchResult: 5")

                    }
                }



                // Handle lifecycle events
                lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onCreate(owner: LifecycleOwner) {
                        mapViewInstance.onCreate(null)
                    }

                    override fun onStart(owner: LifecycleOwner) {
                        mapViewInstance.onStart()
                    }

                    override fun onResume(owner: LifecycleOwner) {
                        mapViewInstance.onResume()
                    }

                    override fun onPause(owner: LifecycleOwner) {
                        mapViewInstance.onPause()
                    }

                    override fun onStop(owner: LifecycleOwner) {
                        mapViewInstance.onStop()
                    }

                    override fun onDestroy(owner: LifecycleOwner) {
                        mapViewInstance.onDestroy()
                    }
                })

                mapViewInstance.loadMapAsync(object : MapView.OnMapReadyCallback {
                    override fun onMapReady(controller: MapController?) {
                        if (controller != null) {
                            mapController = controller
                        }

                        controller?.apply {
                            removeCurrentLocationMarker()
                            setMaxTilt(85F)
                            getUiSettings()?.showZoomControls = false
                            getUiSettings()?.showMyLocationButton (false)
                            getUiSettings()?.showCompass = false
                            
                            setMyLocationEnabled(true, MapController.MyLocationArg.ZOOM_LOCATION_ON_FIRST_FIX)

                            setOnCameraChangeEndListener(object : MapController.OnCameraChangeEndListener {
                                override fun onCameraChangeEnd(cameraPosition: CameraPosition?) {
                                    coroutineScope.launch {
                                        // Switch to IO dispatcher for background work
                                        withContext(Dispatchers.IO) {

                                            if (cameraPosition != null) {
                                                searchLngLat = LngLat(cameraPosition.position.longitude, cameraPosition.position.latitude)
                                            }

                                            val newAddress = cameraPosition?.position?.let { it1 -> String.format("%.4f", it1.latitude) } + " ; " +
                                                    cameraPosition?.position?.let { it1 -> String.format("%.4f", it1.longitude) }

                                            withContext(Dispatchers.Main) {
                                                address = newAddress

                                                if (cameraPosition != null) {
                                                    searchManager!!.requestReverse(
                                                        Params.builder()
                                                            .location(LngLat(cameraPosition.position.latitude, cameraPosition.position.longitude))
                                                            .build(), searchResultListener as OnSearchResult
                                                    )
                                                }
                                            }
                                        }

                                    }
                                }
                            })


                        }
                    }
                })


                mapView = mapViewInstance
                mapViewInstance
            },
            modifier = Modifier.fillMaxSize()
        )



        //Pin Drop Image
        Image(
            painter = painterResource(id = R.drawable.ic_pin_drop),
            contentDescription = "Pin Drop",
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.Center)
        )

        // Column to hold Zoom In, Zoom Out, and Locate Me buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between buttons
        ){
            // Zoom In Button
            FloatingActionButton(
                onClick = {
                    mapController?.apply {
                        val currentZoom = zoom
                        setZoomBy(currentZoom + 1.0f)
                    }
                },
                containerColor = Color.White,
                contentColor = iconColor
            ) {
                Icon(
                   painter = painterResource(id = R.drawable.ic_plus), contentDescription = "Zoom In")
            }

            // Zoom Out Button
            FloatingActionButton(
                onClick = {
                    mapController?.apply {
                        val currentZoom = zoom
                        setZoomBy(currentZoom - 1.0f)
                    }
                },
                containerColor = Color.White,
                contentColor = iconColor
            ) {
                Icon(
                   painter = painterResource(id = R.drawable.ic_minus), contentDescription = "Zoom Out")
            }

            // Locate Me Button
            FloatingActionButton(
                onClick = {
                    mapController?.let { controller ->
                        val myLocation = controller.getMyLocation(mapView)
                        if (myLocation != null) {
                            val cameraPosition = CameraPosition(
                                controller,
                                com.tplmaps3d.LngLat(myLocation.longitude, myLocation.latitude),
                                14.0f,
                                0.0f,
                                0.0f
                            )
                            controller.animateCamera(cameraPosition, 1000)
                        } else {
                            // Handle case when location is not available
                        }
                    }
                },
                containerColor = Color.White,
                contentColor = iconColor
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_near_me), contentDescription = "Locate Me")
            }
        }



        Column(modifier = Modifier.fillMaxWidth()) {
            //Top TextView
            CustomCardView(address = address)

            // Search TextField
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                elevation = CardDefaults.elevatedCardElevation(10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Transparent)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        if (query.isNotEmpty()) {
                            searchManager?.requestOptimizeSearch(
                                Params.builder().query(query)
                                    .location(searchLngLat)
                                    .build(), searchResultListener as OnSearchResult
                            )
                            isSearch = true
                        } else {
                            searchResults = emptyList()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                    ,
                    placeholder = {
                        Text(text = "Search for places...")
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "Search Icon"
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent, // Set to transparent since the Card handles the background
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
            }


            // Display search results below the TextField only if the query is not empty
            if (searchQuery.isNotEmpty() && searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    elevation = CardDefaults.elevatedCardElevation(10.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.Transparent)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        items(searchResults) { place ->
                            Text(
                                text = place.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val name = place.name
                                        address = name
                                        Toast.makeText(context, place.name, Toast.LENGTH_SHORT).show()

                                        mapController?.setLngLatZoom(
                                            com.tplmaps3d.LngLat(place.x.toDouble(), place.y.toDouble()), 15.0f
                                        )
                                        searchResults=emptyList()


                                        // Clear the TextField
                                        searchQuery = ""

                                        // Hide keyboard and clear focus
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                    .padding(8.dp),
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun CustomCardView(address: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
        ,
        elevation = CardDefaults.elevatedCardElevation(10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Transparent) // Equivalent to transparent stroke

    ) {
        Column(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Selected Address:",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = address,
                color = Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

