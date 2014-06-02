package co.uk.tusksolutions.tchat.android.xmpp;

import java.util.Collection;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;

import android.util.Log;

public class XMPPPresenceManager {

	static final String TAG = "XMPPPresenceManager";

	Presence presence;
	Roster roster;

	public XMPPPresenceManager(Connection connection) {
		try {
			/**
			 * Tell the server we are online
			 */
			presence = new Presence(Presence.Type.available);
			connection.sendPacket(presence);

			/**
			 * Request and process our roster
			 */
			roster = connection.getRoster();
			processRosterEntries(roster.getEntries());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Empty signature **/
	public XMPPPresenceManager() {
	}

	private void processRosterEntries(Collection<RosterEntry> entries) {
		for (RosterEntry entry : entries) {
			Log.i(TAG, "--------------------------------------");
			Log.i(TAG, "RosterEntry " + entry);
			Log.i(TAG, "User: " + entry.getUser());
			Log.i(TAG, "Name: " + entry.getName());
			Log.i(TAG, "Status: " + entry.getStatus());
			Log.i(TAG, "Type: " + entry.getType());

			Presence entryPresence = roster.getPresence(entry.getUser());
			Log.i(TAG, "Presence Status: " + entryPresence.getStatus());
			Log.i(TAG, "Presence Type: " + entryPresence.getType());

			Presence.Type type = entryPresence.getType();
			if (type == Presence.Type.available)
				Log.i(TAG, "Presence AVIALABLE");
			Log.i(TAG, "Presence : " + entryPresence);
		}
	}

}
