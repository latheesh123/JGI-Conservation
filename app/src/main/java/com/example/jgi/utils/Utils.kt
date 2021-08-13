package com.example.jgi.utils
class Utils {

    companion object {

        const val arcGIS_api_key="AAPKa63a869d6cc84e2da6e80cc974cd1e4d0kz8lF9wefoTfCV_uOC60twE7aEGKpYIdc2u9qc6SOvjEd9hTCSLBfJzGxzcOVh6"
        const val service_url="https://services3.arcgis.com/df7XtT0Re4z8S561/arcgis/rest/services/Chimpanzee/FeatureServer"
        fun getMiles(distance: Int):Double
        {
            val number=distance*0.00062137112
            val roundoff=Math.round(number *1000.0)/1000.0
            return roundoff
        }

    }

}