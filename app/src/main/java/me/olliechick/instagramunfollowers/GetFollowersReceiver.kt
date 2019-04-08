package me.olliechick.instagramunfollowers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import me.olliechick.instagramunfollowers.Util.Companion.TAG
import me.olliechick.instagramunfollowers.Util.Companion.getCurrentUnfollowers
import me.olliechick.instagramunfollowers.Util.Companion.openInstagramAccountIntent
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.time.OffsetDateTime


/**
 * This will be triggered every X hours.
 * It checks if there any new unfollowers, and notifies the user for each one.
 */
class GetFollowersReceiver : BroadcastReceiver() {

    private var unfLists: MutableList<UnfList> = mutableListOf()

    data class UnfList(var account: Account, var priorUnfollowers: List<Follower>)

    override fun onReceive(context: Context, intent: Intent) {
        context.applicationContext.registerReceiver(
            receiver, IntentFilter(
                GetFollowersService.NOTIFICATION
            )
        )
        doAsync {
            val db = Util.initialiseDb(context)
            val allAccounts = db.accountDao().getAll()
            db.close()

            // Get current unfollowers for all accounts
            unfLists = getPriorUnfollowers(allAccounts, context)

            // Make a list of all the ids
            val ids = mutableListOf<Long>()
            allAccounts.forEach { ids.add(it.id) }

            // Start the service to get the current follower list for each id
            val getFollowersIntent = Intent(context, GetFollowersService::class.java)
            getFollowersIntent.putExtra("ids", ids.toLongArray())
            context.startService(getFollowersIntent)
        }
    }

    private fun getPriorUnfollowers(accounts: List<Account>, context: Context): MutableList<UnfList> {

        val unfLists = mutableListOf<UnfList>()

        accounts.forEach {
            val priorUnfollowers = getCurrentUnfollowers(context, it.id)
            unfLists.add(UnfList(it, priorUnfollowers))
            if (Debug.LOG) Log.i(TAG, "Prior unfollowers: $priorUnfollowers")
        }

        return unfLists
    }

    private fun getNewUnfollowers(context: Context): List<Follower> {

        val newUnfollowers = mutableListOf<Follower>()

        unfLists.forEach {
            val currentUnfollowers = getCurrentUnfollowers(context, it.account.id)
            if (Debug.LOG) Log.i(TAG, "Current unfollowers: $currentUnfollowers")

            newUnfollowers.addAll(currentUnfollowers.minus(it.priorUnfollowers))
            if (Debug.LOG) Log.i(TAG, "New unfollowers: $newUnfollowers")
        }

        // TEST UNFOLLOWER:
        newUnfollowers.add(
            Follower(
                3,
                OffsetDateTime.now().minusHours(1),
                "olliechickdev",
                "Ollie Chick",
                32534
            )
        )

        return newUnfollowers
    }

    private fun sendUnfollowerNotification(unfollower: Follower, i: Int, context: Context) {
        val intent = openInstagramAccountIntent(unfollower.username)
        val pendingIntent = intent.run {
            PendingIntent.getActivity(context, 0, this, 0)
        }

        val notification = Notification.Builder(context, Notification.CATEGORY_SOCIAL).run {
            setSmallIcon(R.drawable.triangle_outline)
            setContentTitle(context.getString(R.string.just_unfollowed_you, unfollower.name, unfollower.username))
            setContentText(context.getString(R.string.tap_to_go_to_profile))
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            build()
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(i, notification)

    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Debug.LOG) Log.i(TAG, "Loud and clear.")
            val receiver = this

            doAsync {
                val newUnfollowers = getNewUnfollowers(context)
                newUnfollowers.forEachIndexed { i, elem -> sendUnfollowerNotification(elem, i, context) }
                context.unregisterReceiver(receiver)
            }
        }
    }

}
