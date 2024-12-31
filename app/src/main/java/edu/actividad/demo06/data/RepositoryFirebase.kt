package edu.actividad.demo06.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Responsible for managing the data of the application coming from Firebase.
 *
 * @property TAG The tag for the log.
 * @property db The Firestore database.
 * @property collectionPath The path of the collection in Firestore.
 * @property namespace The namespace of the documents in Firestore.
 * @author Víctor Lamas
 */
class RepositoryFirebase {
    private val TAG = RepositoryFirebase::class.java.simpleName
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionPath = "demo06"
    private val namespace: String = "Víctor Lamas"

    /**
     * Creates a document in Firebase.
     */
    fun createDocument() {
        val docRef = db.collection("demo06").document(namespace)

        // Check if the document exists
        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                Log.i(TAG, "createdDocument: Document data: ${document.data}")
            } else {
                Log.i(TAG, "createDocument: No such document")
                // Create a empty document with the namespace as the document ID
                docRef.set(mapOf("cities" to emptyList<Map<String, String>>())) // Empty
                    .addOnSuccessListener {
                        Log.i(TAG, "createDocument: Added with ID: $namespace")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "createDocument: Error adding document", e)
                    }
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "createDocument: get failed with ", exception)
        }
    }

    /**
     * Add city to array in a document in Firebase.
     *
     * @param city The name of the city.
     * @param countryCode The country code of the city.
     */
    fun addCity(city: String, countryCode: String) {
        val newCity: List<Map<String, String>> = listOf(
            mapOf("name" to city, "countryCode" to countryCode)
        )

        val docRef = db.collection("demo06").document(namespace)
        // The destructuring operator (*) is used to pass each element of the
        // list as an individual argument.
        docRef.update("cities", FieldValue.arrayUnion(*newCity.toTypedArray()))
            .addOnSuccessListener {
                Log.i(TAG, "addCity: DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addCity: Error updating document", e)
            }
    }

    /**
     * Get array of cities from all documents in Firebase.
     *
     * @return The list of all together visited cities and their visualizations.
     */
    fun fetchArrayAllCitiesDocs(): Flow<List<Map<Map<String, String>, Int>>> = callbackFlow {
        val listenerRegistration = db.collection(collectionPath)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e(TAG, "fetchArrayAllCitiesDocs: Listen failed.", e)
                    close(e) // Close the flow with the exception.
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val globalCount = mutableMapOf<Map<String, String>, Int>()

                    snapshots.documents.forEach { document ->
                        val cities: List<Map<String, String>> =
                            (document.get("cities") as? List<*>)
                                ?.mapNotNull { it as? Map<*, *> }
                                ?.map { cityMap ->
                                    cityMap.filterKeys { it is String }
                                        .filterValues { it is String }
                                        .mapKeys { it.key as String }
                                        .mapValues { it.value as String }
                                } ?: emptyList()

                        // Group by “name” and “countryCode” and count occurrences.
                        val cityCount = cities.groupingBy { it }.eachCount()

                        // Combines local counts on the global map.
                        cityCount.forEach { (cityKey, count) ->
                            globalCount[cityKey] = globalCount.getOrDefault(cityKey, 0) + count
                        }
                    }

                    // Converts the global map into the required list.
                    val result = globalCount.map { mapOf(it.key to it.value) }
                    trySend(result) // Send the list to the flow
                    Log.i(TAG, "fetchArrayAllCitiesDocs: Result -> $result")
                } else {
                    Log.i(TAG, "fetchArrayAllCitiesDocs: No se encontraron documentos.")
                    trySend(emptyList()) // Sends empty list to the flow.
                }
            }

        // Closes the listener when the flow is closed
        awaitClose { listenerRegistration.remove() }
    }
}