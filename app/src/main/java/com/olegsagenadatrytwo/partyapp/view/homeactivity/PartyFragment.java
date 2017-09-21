package com.olegsagenadatrytwo.partyapp.view.homeactivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.olegsagenadatrytwo.partyapp.R;
import com.olegsagenadatrytwo.partyapp.customviews.AutoResizeTextView;
import com.olegsagenadatrytwo.partyapp.model.custompojos.Party;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.UUID;

public class PartyFragment extends Fragment implements ChildEventListener{

    public static final String TAG = "PartyFragment";
    private static final String PARTY_ID = "party_id";
    private List<Party> parties;
    private Party party;
    private AutoResizeTextView tvPartyName;
    private AutoResizeTextView tvDescription;
    private ImageView ivLogo;
    private DatabaseReference partiesReference;
    private Context context;

    public static PartyFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(PARTY_ID, id);
        PartyFragment fragment = new PartyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get the id of the event that were are trying to display
        UUID id = (UUID) getArguments().getSerializable(PARTY_ID);
        //get the singleTon that holds the list of the events
        PartyLabSingleTon partySingleton = PartyLabSingleTon.getInstance(getActivity());
        //get the list of the events from the singleton
        parties = partySingleton.getEvents();
        //find the specific event based on the id
        party = getParty(id);

        this.context = getActivity();

        partiesReference = FirebaseDatabase.getInstance().getReference("parties");
        partiesReference.addChildEventListener(this);
        setHasOptionsMenu(true);
    }

    //this method will return the Event from the list of events based on id of the event passed
    public Party getParty(UUID id) {
        for (int i = 0; i < parties.size(); i++) {
            if (parties.get(i).getId() == id) {
                return parties.get(i);
            }
        }
        return null;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v        = inflater.inflate(R.layout.party_card_item2, container, false);
        tvPartyName   = v.findViewById(R.id.tvPartyType);
        tvDescription = v.findViewById(R.id.tvPartyDescription);
        ivLogo        = v.findViewById(R.id.ivPartyHeader);

        //if the party is not null than set the ImageViews and TextViews according to the Event
        if (party != null) {
            tvPartyName.setText(party.getPartyName());
            tvDescription.setText(party.getDescription());
            loadPartyImageGlide();
        }
        return v;
    }

    private void loadPartyImagePicasso() {
        Log.d(TAG, "loadPartyImagePicasso: ");
        if(party.getImageURL() != null){
            Picasso.with(context)
                    .load(party.getImageURL())
                    .into(ivLogo);
        }

    }

    private void loadPartyImageGlide() {

        Log.d(TAG, "loadPartyImageGlide: ");
        if(party.getImageURL() != null) {
            if(context != null) {
                Log.d(TAG, "loadPartyImageGlide: " + "getActivity() was not null");
                Glide.with(context)
                        .load(party.getImageURL())
                        .into(ivLogo);
            }else{
                Log.d(TAG, "loadPartyImageGlide: " + "getActivity() was null gonna try picasso");
                loadPartyImagePicasso();
            }
        }else {
            Log.d("test", "loadPartyImageGlide: " + ivLogo);
            ivLogo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.partylogo));
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        final Party partyChanged = dataSnapshot.getValue(Party.class);
        partyChanged.setId(UUID.fromString(dataSnapshot.getKey()));

        //if the party that was changed is the one on the screen update the changes live
        if(partyChanged.getId().toString().equals(party.getId().toString())) {
            //get reference to storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://partyapp-fc6fb.appspot.com/");

            //download image
            storageRef.child("images/" + partyChanged.getId() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    party.setImageURL(uri.toString());
                    tvPartyName.setText(partyChanged.getPartyName());
                    tvDescription.setText(partyChanged.getDescription());
                    loadPartyImageGlide();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    tvPartyName.setText(partyChanged.getPartyName());
                    tvDescription.setText(partyChanged.getDescription());
                }
            });
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}