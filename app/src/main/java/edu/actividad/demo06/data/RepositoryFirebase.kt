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
    private val namespace: String = "VLT"

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

    // Get array of cities from a document in Firebase
    fun fetchArrayCities(): Flow<List<Map<String, String>>> = callbackFlow {
        val listenerRegistration = db.collection("demo06").document(namespace)
            .addSnapshotListener { documentSnapshot, e ->
                if (e != null) {
                    Log.e(TAG, "fetchArrayCities: get failed with ${e.message}")
                    close(e) // Close the flow with an exception
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.i(TAG, "fetchArrayCities: Data: ${documentSnapshot.data}")

                    // 1. as? List<*>: Realiza un casting seguro a una lista genérica
                    //      (List<*>) para evitar errores en tiempo de ejecución.
                    // 2. mapNotNull filtra elementos que no sean mapas, asegurando
                    //      que solo procesas datos válidos
                    // 3. filterKeys y filterValues filtran las claves y los valores
                    //      para garantizar que sean del tipo String.
                    // 4. mapKeys y mapValues realizan el casting seguro de las claves
                    //      y valores a String.
                    val cities: List<Map<String, String>> =
                        (documentSnapshot.get("cities") as? List<*>)
                            ?.mapNotNull { it as? Map<*, *> } // Convertir cada elemento
                            ?.map { cityMap ->
                                cityMap.filterKeys { it is String }
                                    .filterValues { it is String }
                                    .mapKeys { it.key as String }
                                    .mapValues { it.value as String }
                            } ?: emptyList()

                    trySend(cities) // Send data to the flow
                } else {
                    Log.e(TAG, "fetchArrayCities: No such document")
                    trySend(emptyList()) // Send empty list to the flow
                }
            }
        // Close the listener when the flow is closed
        awaitClose { listenerRegistration.remove() }
    }
}