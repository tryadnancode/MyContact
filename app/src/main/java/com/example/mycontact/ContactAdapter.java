package com.example.mycontact;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private Context mContext;
    private ArrayList<Contact> contactList;

    public ContactAdapter(Context context, ArrayList<Contact> List) {
        mContext = context;
        contactList = List;
    }

    @NonNull
    @Override
    public ContactAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ContactViewHolder holder, int position) {
        Contact currentContact = contactList.get(position);
        if(currentContact.getPhotoUri() != null){
            holder.contactImage.setImageURI(Uri.parse(currentContact.getPhotoUri()));
        }else{
            holder.contactImage.setImageResource(R.drawable.person);
        }
        holder.contactName.setText(currentContact.getName() + "\n" + currentContact.getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {

        ImageView contactImage;
        TextView contactName;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactImage = itemView.findViewById(R.id.contact_image);
            contactName = itemView.findViewById(R.id.contact_name);
        }
    }
}
