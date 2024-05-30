package com.example.mycontact;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ImageButton add;
        ImageView menu;
    RecyclerView recyclerView;
    private ContactAdapter adapter;
    private static final int REQUEST_CONTACT_PERMISSION = 1;
    private static final int REQUEST_CONTACT_PICK = 2;
    private final ArrayList<Contact> contactList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findId();
        onclick();
        recycler();
    }
    private void recycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(this, contactList);
        recyclerView.setAdapter(adapter);
    }
    private void findId() {
        add = findViewById(R.id.add);
        recyclerView = findViewById(R.id.contact_list);
        menu = findViewById(R.id.menu);
    }
    private void onclick() {
        add.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_CONTACT_PERMISSION);
            } else {
                pickContact();
            }
        });
        menu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                item.getItemId();
                if (item.getItemId()==R.id.profile) {
                    Fragment myFragment = new MenuFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main, myFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.home) {
                    Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                } else if(item.getItemId() == R.id.setting){
                    Toast.makeText(MainActivity.this, "Setting", Toast.LENGTH_SHORT).show();
                }
                return false;
            });
            popupMenu.show();
        });
    }
    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACT_PICK);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CONTACT_PICK && resultCode == RESULT_OK) {
            if (data != null) {
                Uri contactUri = data.getData();
                String[] projection = {ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                        ContactsContract.Contacts.PHOTO_URI
                };
                try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        int hasPhoneNumberIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                        int photoUriIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI);
                        String contactId = cursor.getString(idIndex);
                        String contactName = cursor.getString(nameIndex);
                        String photoUri = cursor.getString(photoUriIndex);
                        int hasPhoneNumber = cursor.getInt(hasPhoneNumberIndex);

                        if (hasPhoneNumber > 0) {
                            getPhoneNumber(contactId, contactName, photoUri);
                        } else {
                            Toast.makeText(this, "Selected contact has no phone number", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }
    private void getPhoneNumber(String contactId, String contactName, String photoUri) {
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor phoneCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );
        if (phoneCursor != null && phoneCursor.moveToFirst()) {
            int numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String phoneNumber = phoneCursor.getString(numberIndex);
            if (!isContactExists(contactName)) {
                contactList.add(new Contact(contactName, phoneNumber, photoUri));
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Contact already exists", Toast.LENGTH_SHORT).show();
            }
            phoneCursor.close();
        }
    }
    private boolean isContactExists(String contactName) {
        for (Contact contact : contactList) {
            if (contact.getName().equals(contactName)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickContact();
            } else {
                Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }
}