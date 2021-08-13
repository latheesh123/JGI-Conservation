package com.example.jgi.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.arcgisservices.FeatureServiceSessionType
import com.esri.arcgisruntime.arcgisservices.ServiceVersionParameters
import com.esri.arcgisruntime.arcgisservices.VersionAccess
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.GeoElement
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.esri.arcgisruntime.security.OAuthConfiguration
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.example.jgi.BaseFragment
import com.example.jgi.R
import com.example.jgi.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.display_map_fragment.*
import kotlinx.android.synthetic.main.create_version_dialog.view.*
import java.text.SimpleDateFormat
import java.util.*


class DisplayMapFragment : BaseFragment() {

    private lateinit var mPointSymbol: SimpleMarkerSymbol
    private lateinit var mLineSymbol: SimpleLineSymbol
    private lateinit var mFillSymbol: SimpleFillSymbol
    private lateinit var mSketchEditor: SketchEditor
    private lateinit var mGraphicsOverlay: GraphicsOverlay

    private lateinit var mPointButton: ImageButton
    private lateinit var mMultiPointButton: ImageButton
    private lateinit var mPolylineButton: ImageButton
    private lateinit var mPolygonButton: ImageButton
    private lateinit var mFreehandLineButton: ImageButton
    private lateinit var mFreehandPolygonButton: ImageButton
    private lateinit var sketchGeometry: Geometry
    private lateinit var mCallout: Callout
    private lateinit var createdVersionName: String
    private lateinit var selectedFeature: Feature
    private lateinit var serviceFeatureTable: ServiceFeatureTable
    private lateinit var serviceGeodatabase: ServiceGeodatabase
    private lateinit var featureLayer: FeatureLayer

    private lateinit var sendFloatingButton: FloatingActionButton
    private lateinit var CreateVersionFloatingButton: FloatingActionButton
    private lateinit var confirmFloatingButton: FloatingActionButton
    private lateinit var listFloatingButton: FloatingActionButton
    private lateinit var version_label: TextView

    private var isAllVisible: Boolean = false

    companion object {
        fun newInstance() = DisplayMapFragment()
    }

    private lateinit var viewModel: DisplayMapViewModel

    override fun onBackPressed(): Boolean {
        findNavController().navigate(R.id.destination_showitems)
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.display_map_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serviceGeodatabase =
            ServiceGeodatabase(Utils.service_url)

        val oAuthConfiguration = OAuthConfiguration(
            getString(R.string.portal_url),
            getString(R.string.oauth_client_id),
            getString(R.string.oauth_redirect_uri) + "://" + getString(R.string.oauth_redirect_host)
        )
        val defaultAuthenticationChallengeHandler =
            DefaultAuthenticationChallengeHandler(requireActivity())
        AuthenticationManager.setAuthenticationChallengeHandler(
            defaultAuthenticationChallengeHandler
        )

        AuthenticationManager.addOAuthConfiguration(oAuthConfiguration)


        val portal = Portal(getString(R.string.portal_url))
        val portalItem = PortalItem(portal, getString(R.string.webmap_world_traffic_id))
        mapView.apply {
            map = ArcGISMap(portalItem)
        }



        mPointSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, -0x10000, 20F)
        mLineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, -0x7800, 4F)
        mFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.CROSS, 0x40FFA9A9, mLineSymbol)

        setupMap()

        mGraphicsOverlay = GraphicsOverlay()
        mapView.graphicsOverlays.add(mGraphicsOverlay)

        mSketchEditor = SketchEditor()
        mapView.setSketchEditor(mSketchEditor)

        mPointButton = view.findViewById(R.id.pointButton);
        mMultiPointButton = view.findViewById(R.id.pointsButton);
        mPolylineButton = view.findViewById(R.id.polylineButton);
        mPolygonButton = view.findViewById(R.id.polygonButton);
        mFreehandLineButton = view.findViewById(R.id.freehandLineButton);
        mFreehandPolygonButton = view.findViewById(R.id.freehandPolygonButton);

        version_label = view.findViewById(R.id.version_name)
        confirmFloatingButton = view.findViewById(R.id.confirm_edit)
        listFloatingButton = view.findViewById(R.id.all_options)
        sendFloatingButton = view.findViewById(R.id.send_floating_action)
        CreateVersionFloatingButton = view.findViewById(R.id.create_version)

        confirmFloatingButton.visibility = View.GONE
        sendFloatingButton.visibility = View.GONE
        CreateVersionFloatingButton.visibility = View.GONE

        listFloatingButton.setOnClickListener {

            if (!isAllVisible) {
                confirmFloatingButton.visibility = View.VISIBLE
                sendFloatingButton.visibility = View.VISIBLE
                CreateVersionFloatingButton.visibility = View.VISIBLE
                isAllVisible = true
            } else {
                confirmFloatingButton.visibility = View.GONE
                sendFloatingButton.visibility = View.GONE
                CreateVersionFloatingButton.visibility = View.GONE
                isAllVisible = false

            }
        }

        sendFloatingButton.setOnClickListener {
            sendMap()
        }

        confirmFloatingButton.setOnClickListener {
            addFeature(sketchGeometry, serviceFeatureTable)
        }

        CreateVersionFloatingButton.setOnClickListener {

            createVersionDialog()
        }
        mPointButton.setOnClickListener { CreatePoint() }

        mMultiPointButton.setOnClickListener { CreateMultiPoint() }

        mPolylineButton.setOnClickListener { CreatePolyLine() }

        mPolygonButton.setOnClickListener { CreatePolygon() }

        mFreehandLineButton.setOnClickListener { CreateFreehandLine() }

        mFreehandPolygonButton.setOnClickListener { CreateFreeHandPolygon() }

    }

    private fun CreateFreeHandPolygon() {
        resetButtons();
        mFreehandPolygonButton.setSelected(true);
        mSketchEditor.start(SketchCreationMode.FREEHAND_POLYGON);
    }

    private fun CreateFreehandLine() {
        resetButtons();
        mFreehandLineButton.setSelected(true);
        mSketchEditor.start(SketchCreationMode.FREEHAND_LINE);
    }

    private fun CreatePolygon() {
        resetButtons();
        mPolygonButton.setSelected(true);
        mSketchEditor.start(SketchCreationMode.POLYGON);
    }

    private fun CreatePolyLine() {
        resetButtons();
        mPolylineButton.setSelected(true);
        mSketchEditor.start(SketchCreationMode.POLYLINE);
    }

    private fun CreateMultiPoint() {
        resetButtons();
        mMultiPointButton.setSelected(true);
        mSketchEditor.start(SketchCreationMode.MULTIPOINT);
    }

    private fun CreatePoint() {
        resetButtons();
        mPointButton.setSelected(true);
        mSketchEditor.start(SketchCreationMode.POINT);
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.undo_redo_stop_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.undo -> undo()
            R.id.redo -> redo()
            R.id.stop -> stop()
            R.id.refresh -> setupMap()

        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendMap() {
        findNavController().navigate(R.id.destination_confirm)
    }

    private fun addFeature(geometry: Geometry, featureTable: ServiceFeatureTable) {

        // create default attributes for the feature
        hashMapOf<String, Any>(
            "BINOMIAL" to "Pan troglodytes",
            "SUBSPECIES" to "trolodytes"
        ).let { attributes ->
            // creates a new feature using default attributes and point
            featureTable.createFeature(attributes, geometry)
        }.let { feature ->
            // check if feature can be added to feature table
            if (featureTable.canAdd()) {
                // add the new feature to the feature table and to server
                featureTable.addFeatureAsync(feature).addDoneListener { applyEdits(featureTable) }
            } else {
                // logToUser(true, getString(R.string.error_cannot_add_to_feature_table))
            }
        }

    }

    private fun stop() {
        if (!mSketchEditor.isSketchValid) {
            reportNotValid(requireView())
            mSketchEditor.stop()
            resetButtons()
            return
        }

        // get the geometry from sketch editor
        sketchGeometry = mSketchEditor.geometry
        mSketchEditor.stop()
        resetButtons()

        if (sketchGeometry != null) {

            // create a graphic from the sketch editor geometry
            val graphic = Graphic(sketchGeometry)

            // assign a symbol based on geometry type
            if (graphic.geometry.geometryType == GeometryType.POLYGON) {
                graphic.symbol = mFillSymbol
            } else if (graphic.geometry.geometryType == GeometryType.POLYLINE) {
                graphic.symbol = mLineSymbol
            } else if (graphic.geometry.geometryType == GeometryType.POINT ||
                graphic.geometry.geometryType == GeometryType.MULTIPOINT
            ) {
                graphic.symbol = mPointSymbol
            }

            // add the graphic to the graphics overlay
            mGraphicsOverlay.graphics.add(graphic)

        }
    }

    private fun redo() {
        if (mSketchEditor.canRedo()) {
            mSketchEditor.redo();
        }
    }

    private fun undo() {
        if (mSketchEditor.canUndo()) {
            mSketchEditor.undo();
        }
    }

    private fun resetButtons() {
        mPointButton.isSelected = false
        mMultiPointButton.isSelected = false
        mPolylineButton.isSelected = false
        mPolygonButton.isSelected = false
        mFreehandLineButton.isSelected = false
        mFreehandPolygonButton.isSelected = false
    }

    override fun onPause() {
        AuthenticationManager.CredentialCache.clear()
        AuthenticationManager.clearOAuthConfigurations()
        mapView.pause()

        super.onPause()
    }

    override fun onResume() {
        mapView.resume()
        super.onResume()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()

    }

    private fun setupMap() {


        ArcGISRuntimeEnvironment.setApiKey(Utils.arcGIS_api_key)
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        mGraphicsOverlay = GraphicsOverlay()

        // create the service feature table
//        serviceFeatureTable =
//            ServiceFeatureTable("https://services3.arcgis.com/df7XtT0Re4z8S561/arcgis/rest/services/Chimpanzee/FeatureServer/0")
//
//
        serviceGeodatabase.loadAsync()
        serviceGeodatabase.addDoneLoadingListener {
            if (serviceGeodatabase.loadStatus != LoadStatus.LOADED) {
                serviceGeodatabase.loadError?.let {
                    Log.e("Tag", "Service Geodatabase failed to load: ${it.cause}")
                }
                return@addDoneLoadingListener
            }
            serviceFeatureTable = serviceGeodatabase.getTable(0)


            // create the feature layer using the service feature table
            featureLayer = FeatureLayer(serviceFeatureTable)
            map.addLoadStatusChangedListener {
                val maploadStatus: String
                maploadStatus = it.newLoadStatus.name

                when (maploadStatus) {
                    "LOADING" -> {
                        Toast.makeText(requireContext(), "loading", Toast.LENGTH_SHORT).show()
                    }
                    "FAILED_TO_LOAD" -> {
                        Toast.makeText(requireContext(), "Failed to load", Toast.LENGTH_SHORT)
                            .show()

                    }
                    "NOT_LOADED" -> {
                        Toast.makeText(requireContext(), "Not loaded", Toast.LENGTH_SHORT).show()

                    }
                    "LOADED" -> {
                        Toast.makeText(requireContext(), "Loaded", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Error loading map", Toast.LENGTH_SHORT)
                            .show()

                    }
                }
            }
            mCallout = mapView.callout

            map.operationalLayers.add(featureLayer)
            mapView.map = map
            mapView.setViewpoint(Viewpoint(7.9465, 1.0232, 20000000.0))
        }
        mapView.setOnTouchListener(object :
            DefaultMapViewOnTouchListener(requireContext(), mapView) {

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                // remove any existing callouts
                if (mCallout.isShowing) {
                    mCallout.dismiss()
                }
                // get the point that was clicked and convert it to a point in map coordinates
                val screenPoint = Point(Math.round(e.x), Math.round(e.y))
                // create a selection tolerance
                val tolerance = 10
                // use identifyLayerAsync to get tapped features
                val identifyLayerResultListenableFuture = mMapView
                    .identifyLayerAsync(featureLayer, screenPoint, tolerance.toDouble(), false, 1)
                identifyLayerResultListenableFuture.addDoneListener {
                    try {
                        val identifyLayerResult =
                            identifyLayerResultListenableFuture.get()
                        // create a textview to display field values
                        val deleteButton = Button(requireContext())

                        val calloutContent =
                            TextView(requireContext())
                        calloutContent.setTextColor(Color.BLACK)
                        calloutContent.isSingleLine = false
                        calloutContent.isVerticalScrollBarEnabled = true
                        calloutContent.scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
                        calloutContent.movementMethod = ScrollingMovementMethod()
                        calloutContent.setLines(5)
                        for (element in identifyLayerResult.elements) {
                            val feature: Feature = element as Feature
                            // create a map of all available attributes as name value pairs
                            val attr: Map<String, Any> = feature.getAttributes()
                            val keys = attr.keys
                            for (key in keys) {
                                var value = attr[key]
                                // format observed field value as date
                                if (value is GregorianCalendar) {
                                    val simpleDateFormat =
                                        SimpleDateFormat("dd-MMM-yyyy", Locale.US)
                                    value =
                                        simpleDateFormat.format((value as GregorianCalendar?)?.getTime())
                                }
                                // append name value pairs to text view
                                calloutContent.append("$key | $value\n")
                            }
                            // center the mapview on selected feature
                            val envelope: Envelope = feature.getGeometry().getExtent()
                            mMapView.setViewpointGeometryAsync(envelope, 200.0)
                            // show callout
                            mCallout.location = envelope.center
                            mCallout.content = calloutContent
                            mCallout.show()
                        }
                    } catch (e1: Exception) {
                        Log.e(
                            resources.getString(R.string.app_name),
                            "Select feature failed: " + e1.message
                        )
                    }
                }
                return super.onSingleTapConfirmed(e)
            }

            override fun onLongPress(e: MotionEvent) {
                // remove any existing callouts
                if (mCallout.isShowing) {
                    mCallout.dismiss()
                }
                // get the point that was clicked and convert it to a point in map coordinates
                val screenPoint = Point(Math.round(e.x), Math.round(e.y))
                // create a selection tolerance
                val tolerance = 10
                // use identifyLayerAsync to get tapped features
                val identifyLayerResultListenableFuture = mMapView
                    .identifyLayerAsync(featureLayer, screenPoint, tolerance.toDouble(), false, 1)
                identifyLayerResultListenableFuture.addDoneListener {
                    try {
                        val identifyLayerResult =
                            identifyLayerResultListenableFuture.get()
                        // create a textview to display field values

                        (identifyLayerResult.elements?.firstOrNull() as? Feature)?.let { feature ->
                            mapView.screenToLocation(screenPoint).let {
                                inflateCallout(mapView, feature, it).show()
                            }
                        }
                    } catch (e1: Exception) {
                        Log.e(
                            resources.getString(R.string.app_name),
                            "Select feature failed: " + e1.message
                        )
                    }
                }
                return super.onLongPress(e)
            }
        })

    }

    fun createVersionDialog() {
        // inflate the view and get references to each of its components
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.create_version_dialog, null)
        val createNameEditText = dialogView.createNameEditText
        val createDescriptionEditText = dialogView.createDescriptionEditText
        val createAccessVersionSpinner = dialogView.createAccessVersionSpinner

        // set up the spinner to display options for the VersionAccess parameter for creating a version
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.version_access_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            createAccessVersionSpinner.adapter = adapter
        }

        // set up the dialog
        AlertDialog.Builder(requireContext()).apply {
            setView(dialogView)
            setTitle("Create a new version")
            setNegativeButton("Cancel") { _: DialogInterface, _: Int -> }
            setPositiveButton("Create") { _: DialogInterface, _: Int ->
                // when the user confirms check a name has been entered
                if (createNameEditText.text.toString().isNotEmpty()) {

                    Toast.makeText(requireContext(), "newversioncreated", Toast.LENGTH_SHORT)
                    version_label.text = createNameEditText.text.toString()

                    // create the version with the given parameters
//                    createVersion(
//                        createNameEditText.text.toString(),
//                        VersionAccess.valueOf(createAccessVersionSpinner.selectedItem.toString()),
//                        createDescriptionEditText.text.toString()
//                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        "A version name is required!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.create().show()
    }

    private fun createVersion(
        versionName: String,
        versionAccess: VersionAccess,
        description: String
    ) {
        // create service version parameters with the parameters passed to this method
        val serviceVersionParameters = ServiceVersionParameters().apply {
            name = versionName
            access = versionAccess
            setDescription(description)
        }

        // create the version
        val serviceVersionInfoFuture =
            serviceGeodatabase.createVersionAsync(serviceVersionParameters)
        serviceVersionInfoFuture.addDoneListener {
            // get the new version's name and switch to it
            val serviceVersionInfo = serviceVersionInfoFuture.get()
            createdVersionName = serviceVersionInfo.name
            switchVersion(null)
        }

        // hide the create version button and allow the user to switch versions now
        CreateVersionFloatingButton.visibility = View.GONE
    }

    fun switchVersion(view: View?) {
        // don't switch versions if the new version has not been created yet or the name has not been stored
        if (createdVersionName.isBlank()) {
            val message = "Version names have not been initialized!"
            Log.e("Tag", message)
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }

        // switch between default and created version names
        val versionName = when (serviceGeodatabase.versionName) {
            serviceGeodatabase.defaultVersionName -> createdVersionName
            createdVersionName -> serviceGeodatabase.defaultVersionName
            else -> serviceGeodatabase.defaultVersionName
        }

        // if the user has changed any features
        if (serviceGeodatabase.hasLocalEdits()) {
            // apply those changes
            serviceGeodatabase.applyEditsAsync().addDoneListener {
                try {
                    // switch versions
                    serviceGeodatabase.switchVersionAsync(versionName).addDoneListener {
                        version_label.text = "Current version: ${serviceGeodatabase.versionName}"
                    }
                } catch (e: Exception) {
                    val error = "Failed to switch version: ${e.message}"
                    Log.e("TAG", error)
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            try {
                // switch versions
                serviceGeodatabase.switchVersionAsync(versionName).addDoneListener {
                    version_label.text = "Current version: ${serviceGeodatabase.versionName}"
                }
            } catch (e: Exception) {
                val error = "Failed to switch version: ${e.message}"
                Log.e("TAG", error)
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun inflateCallout(
        mapView: MapView,
        feature: GeoElement,
        point: com.esri.arcgisruntime.geometry.Point
    ): Callout {
        with(LayoutInflater.from(requireContext()).inflate(R.layout.delete_callout, null)) {
            // set OnClickListener for Callout content
            this.findViewById<View>(R.id.calloutViewCallToAction).setOnClickListener {
                // get objectid from feature attributes and pass to function to confirm deletion
                //  confirmDeletion((feature.attributes["ID_NO"].toString()))

                Toast.makeText(
                    requireContext(),
                    feature.attributes["ID_NO"].toString(),
                    Toast.LENGTH_SHORT
                ).show()
                // dismiss callout
                mapView.callout.dismiss()
            }
            // set callout content as inflated View
            mapView.callout.content = this
            // set callout GeoElement as feature at tap location
            mapView.callout.setGeoElement(feature, point)
        }
        return mapView.callout
    }

//
//    private fun confirmDeletion(featureId: String) {
//        ConfirmDeleteFeatureDialog.newInstance(featureId)
//            .show(supportFragmentManager, ConfirmDeleteFeatureDialog::class.java.simpleName)
//    }

    private fun reportNotValid(view: View) {
        val validIf: String
        validIf = if (mSketchEditor.sketchCreationMode == SketchCreationMode.POINT) {
            "Point only valid if it contains an x & y coordinate."
        } else if (mSketchEditor.sketchCreationMode == SketchCreationMode.MULTIPOINT) {
            "Multipoint only valid if it contains at least one vertex."
        } else if (mSketchEditor.sketchCreationMode == SketchCreationMode.POLYLINE
            || mSketchEditor.sketchCreationMode == SketchCreationMode.FREEHAND_LINE
        ) {
            "Polyline only valid if it contains at least one part of 2 or more vertices."
        } else if (mSketchEditor.sketchCreationMode == SketchCreationMode.POLYGON
            || mSketchEditor.sketchCreationMode == SketchCreationMode.FREEHAND_POLYGON
        ) {
            "Polygon only valid if it contains at least one part of 3 or more vertices which form a closed ring."
        } else {
            "No sketch creation mode selected."
        }
        val report = "Sketch geometry invalid:\n$validIf"
        val reportSnackbar =
            Snackbar.make(
                view.findViewById(R.id.toolbarInclude),
                report,
                Snackbar.LENGTH_INDEFINITE
            )
        reportSnackbar.setAction("Dismiss") { view: View? -> reportSnackbar.dismiss() }
        val snackbarTextView =
            reportSnackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackbarTextView.isSingleLine = false
        reportSnackbar.show()
        Log.e("TAG", report)
    }

    private fun applyEdits(featureTable: ServiceFeatureTable) {

        // apply the changes to the server
        featureTable.applyEditsAsync().let { editResult ->
            editResult.addDoneListener {
                try {
                    editResult.get()?.let { edits ->
                        // check if the server edit was successful
                        edits.firstOrNull()?.let {
                            if (!it.hasCompletedWithErrors()) {
                                Log.e("Taf", "sucess")
                            } else {
                                it.error
                            }
                        }
                    }
                } catch (e: ArcGISRuntimeException) {

                }
            }
        }
    }


}