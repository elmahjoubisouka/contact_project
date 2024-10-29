package ma.ensa.contact_project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_CONTACTS = 1;
    private ListView listView;
    private EditText searchEditText;
    private ArrayList<String> contactList;
    private ArrayList<String> filteredContactList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        searchEditText = findViewById(R.id.searchEditText);
        contactList = new ArrayList<>();
        filteredContactList = new ArrayList<>();

        // Vérifiez les permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
        } else {
            loadContacts();
        }

        // Ajoutez un TextWatcher pour filtrer la liste de contacts
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Écouteur de clic pour la liste des contacts
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String contact = filteredContactList.get(position);
            shareContact(contact);
        });
    }

    private void loadContacts() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);

            // Vérifiez si le curseur est valide
            if (cursor != null && cursor.getCount() > 0) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                // Assurez-vous que les colonnes existent
                if (nameIndex != -1 && phoneIndex != -1) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(nameIndex);
                        String phoneNumber = cursor.getString(phoneIndex);
                        if (name != null && phoneNumber != null) {
                            contactList.add(name + ": " + phoneNumber);
                        }
                    }
                    filteredContactList.addAll(contactList);
                    adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredContactList);
                    listView.setAdapter(adapter);
                } else {
                    Toast.makeText(this, "Could not retrieve contact columns", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No contacts found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to retrieve contacts", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void filterContacts(String query) {
        filteredContactList.clear();
        for (String contact : contactList) {
            if (contact.toLowerCase().contains(query.toLowerCase())) {
                filteredContactList.add(contact);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void shareContact(String contact) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, contact);
        startActivity(Intent.createChooser(shareIntent, "Share Contact Using"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}