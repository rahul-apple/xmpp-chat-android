package co.uk.tusksolutions.tchat.android.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import co.uk.tusksolutions.tchat.android.R;
import co.uk.tusksolutions.tchat.android.TChatApplication;
import co.uk.tusksolutions.tchat.android.adapters.GroupFriendsSelectionAdapter;
import co.uk.tusksolutions.tchat.android.api.APIDeleteGroup;
import co.uk.tusksolutions.tchat.android.models.GroupsModel;
import co.uk.tusksolutions.tchat.android.models.RosterModel;
import co.uk.tusksolutions.tchat.android.tasks.RemovePeopleFromGroup;
import co.uk.tusksolutions.tchat.android.tasks.RemovePeopleFromGroup.OnRemoveMUCListener;

public class GroupParticipantsActivity extends ActionBarActivity implements
		OnRemoveMUCListener {

	public static ListView listView;
	public String TAG = "GroupParticipantsActivity";
	private static GroupFriendsSelectionAdapter mAdapter;
	ArrayList<RosterModel> totalSelectedModel, totalUnSelectedModel;
	public Bundle bundle;
	public String groupId;
	public JSONArray participants;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_participants);
		actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("Remove People");

		listView = (ListView) findViewById(R.id.list_view);
		listView.setVerticalScrollBarEnabled(false);
		listView.setHorizontalScrollBarEnabled(false);

		bundle = getIntent().getExtras();

		try {
			prepareListView();
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void prepareListView() throws JSONException {

		if (bundle != null) {
			groupId = bundle.getString("group_id");
			participants = TChatApplication.getGroupsModel().getParticipants(
					groupId);
			if (participants != null) {
				mAdapter = new GroupFriendsSelectionAdapter(
						TChatApplication.getContext(), participants);
				listView.setAdapter(mAdapter);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.group_participants_selection_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.submit_next:

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					handleSelection();

				}
			}, 100);

			break;
		default:
			break;
		}
		return true;
	}

	protected void handleSelection() {
		totalSelectedModel = new ArrayList<RosterModel>();
		totalUnSelectedModel = new ArrayList<RosterModel>();

		for (RosterModel model : GroupFriendsSelectionAdapter.rosterModelCollection) {
			if (model.isSelected()) {
				totalSelectedModel.add(model);
			} else {
				totalUnSelectedModel.add(model);
			}
		}

		if (totalSelectedModel.size() >= 1) {

			new RemovePeopleFromGroup(GroupParticipantsActivity.this, groupId,
					totalSelectedModel, this).execute();

		} else {
			Log.d(TAG, "No selection made");
		}
	}

	@SuppressLint("InlinedApi") @Override
	public void onRemoveMUCSuccess(ArrayList<RosterModel> removedFriendsList) {

		for (RosterModel rosterModel : removedFriendsList) {
			GroupsModel.kickUserFromGroup(groupId, rosterModel.user);
		}

		// Update participants and save to db
		Log.d(TAG, "TOTAL UNSELECTED: " + totalUnSelectedModel);
		if (totalUnSelectedModel.size() >= 1) {

			JSONArray jsonArray = new JSONArray();
			JSONObject participant;

			try {
				for (RosterModel rosterModel : totalUnSelectedModel) {
					participant = new JSONObject();
					participant.put("user_id", rosterModel.user);
					jsonArray.put(participant);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (GroupsModel.updateGroupParticipants(groupId, jsonArray)) {
				try {
					prepareListView();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else {
			if (GroupsModel.deleteGroup(groupId)) {
				APIDeleteGroup deleteGroupApiObj = new APIDeleteGroup();
				deleteGroupApiObj.doDeleteGroup(groupId,
						TChatApplication.getCurrentJid());

				Intent i = new Intent(GroupParticipantsActivity.this,
						MainActivity.class);
				// set the new task and clear flags
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(i);
			}
		}
	}

	@Override
	public void onRemoveMUCFailed() {

		Log.d(TAG, "Remove Failed");
	}

}