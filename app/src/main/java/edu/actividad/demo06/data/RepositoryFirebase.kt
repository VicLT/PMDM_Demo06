package edu.actividad.demo06.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RepositoryFirebase {
    private val TAG = RepositoryFirebase::class.java.simpleName
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionPath = "demo06"
    private val namespace: String = "Víctor Lamas"

    // Create a document in Firebase
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

    // Add city to array in a document in Firebase
    fun addCity(city: String, countryCode: String) {
        val newCity: List<Map<String, String>> = listOf(
            mapOf("name" to city, "countryCode" to countryCode)
        )

        val docRef = db.collection("demo06").document(namespace)
        // Se usa el operador de desestructuración (*) para pasar cada elemento
        // de la lista como un argumento individual.
        docRef.update("cities", FieldValue.arrayUnion(*newCity.toTypedArray()))
            .addOnSuccessListener {
                Log.i(TAG, "addCity: DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addCity: Error updating document", e)
            }
    }

    // Get array of cities from all documents in Firebase
    fun fetchArrayAllCitiesDocs(): Flow<List<Map<Map<String, String>, Int>>> = callbackFlow {
        val listenerRegistration = db.collection(collectionPath)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e(TAG, "fetchArrayAllCitiesDocs: Listen failed.", e)
                    close(e) // Cierra el flujo con la excepción
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

                        // Agrupa por "name" y "countryCode" y cuenta las ocurrencias
                        val cityCount = cities.groupingBy { it }.eachCount()

                        // Combina los conteos locales en el mapa global
                        cityCount.forEach { (cityKey, count) ->
                            globalCount[cityKey] = globalCount.getOrDefault(cityKey, 0) + count
                        }
                    }

                    // Convierte el mapa global en la lista requerida
                    val result = globalCount.map { mapOf(it.key to it.value) }
                    trySend(result) // Envía la lista al flujo
                    Log.i(TAG, "fetchArrayAllCitiesDocs: Result -> $result")
                } else {
                    Log.i(TAG, "fetchArrayAllCitiesDocs: No se encontraron documentos.")
                    trySend(emptyList()) // Envía lista vacía al flujo
                }
            }

        // Cierra el listener cuando el flujo sea cerrado
        awaitClose { listenerRegistration.remove() }
    }
}