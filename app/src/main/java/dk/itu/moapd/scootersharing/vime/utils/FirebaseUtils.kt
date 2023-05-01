package dk.itu.moapd.scootersharing.vime.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import dk.itu.moapd.scootersharing.vime.data.Card
import dk.itu.moapd.scootersharing.vime.data.Ride
import dk.itu.moapd.scootersharing.vime.data.Scooter
import kotlinx.coroutines.tasks.await

fun FirebaseUser.addRide(db: DatabaseReference, ride: Ride) {
    // Getting the scooter
    db.child("scooters").child(ride.scooterId).get().addOnSuccessListener { snapshot ->
        val scooter = snapshot.getValue(Scooter::class.java)

        if (scooter != null) {
            // Adding ride
            this.let { user ->
                val uid = user.let { db.child("rides").child(it.uid).push().key }

                if (uid != null) {
                    db.child("rides").child(user.uid).child(uid).setValue(ride)
                }
            }

            // Updating the scooter
            db.child("scooters").child(ride.scooterId)
                .setValue(Scooter(scooter.name, ride.endLocationAddress!!, ride.endLocationLat!!, ride.endLocationLon!!, scooter.imageUrl, scooter.latestImageUrl, true))
        }

    }
}

fun FirebaseUser.editCard(db: DatabaseReference, card: Card) {
    db.child("cards").child(this.uid).setValue(card)

//    if (uid != null) {
//        db.child("cards").child(uid).setValue(card)
//    }
}

suspend fun FirebaseUser.getCard(db: DatabaseReference): Card? {
    val snapshot = db.child("cards").child(this.uid).get().await()

    return if (snapshot.exists())
        snapshot.getValue(Card::class.java)
    else
        null
}

// Used for creating the 3 default scooters
fun DatabaseReference.addScooter(scooter: Scooter) {
    val uid = this.child("scooters").push().key

    if (uid != null) {
        this.child("scooters").child(uid).setValue(scooter)
    }
}

suspend fun DatabaseReference.getIdsToScooters(): Map<String, Scooter> {
    val map = mutableMapOf<String, Scooter>()
    val snapshot = this.child("scooters").get().await()
    snapshot.children.forEach { scooterSnap ->
        scooterSnap.getValue(Scooter::class.java)?.let { scooter ->
            map[scooterSnap.key!!] = scooter
        }
    }
    return map
}

fun StorageReference.loadScooterImageInto(ctx: Context, view: ImageView) {
    this.downloadUrl.addOnSuccessListener {
        Glide.with(ctx)
            .load(it)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .into(view)
    }
}