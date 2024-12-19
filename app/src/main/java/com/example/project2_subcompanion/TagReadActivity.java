package com.example.project2_subcompanion;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.contentpager.content.Query;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Blake Hardy
 * Reads cards that are presented to device and extracts the student ID. Sets their account as checked in. Also provides a typing version and as a backup.
 */
public class TagReadActivity extends AppCompatActivity {

    private NfcAdapter myNfcAdapter;

    TextView tagText, backupText;
    Button enterButton;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tagread);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        backupText = findViewById(R.id.backupEditText);
        enterButton = findViewById(R.id.enterButton);
        enterButton.setOnClickListener(v -> {
            String studentID = backupText.getText().toString();
            checkUserIn(studentID);
        });
        myNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        tagText = findViewById(R.id.statusTextView);
    }

//    private void checkUserIn(String studentID) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Query to find the user document with the matching studentID
//        Query query = db.collection("users").whereEqualTo("studentID",Integer.parseInt(studentID) );
//        tagText.setText("Checking in...");
//        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    if (!task.getResult().isEmpty()) {
//                        // User document found
//                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
//                        boolean checkedInAD = document.getBoolean("checkedInAD");
//
//                        if (!checkedInAD) {
//                            // User is not checked in, update to true
//                            document.getReference().update("checkedInAD", true)
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            tagText.setText("Ready To Scan...");
//                                            Toast.makeText(TagReadActivity.this, "User Successfully checked in", Toast.LENGTH_SHORT).show();
//                                        }
//                                    })
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Toast.makeText(TagReadActivity.this, "User Check In Failed", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                        } else {
//
//                            // User is already checked in, handle accordingly
//                            Toast.makeText(TagReadActivity.this, "User ALREADY CHECKED IN", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        // User document not found, handle accordingly
//                        Toast.makeText(TagReadActivity.this, "No Account Found", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    // Handle the error
//                }
//            }
//        });
//    }

    private void checkUserIn(String studentID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query to find the user document with the matching studentID
        Query query = db.collection("users").whereEqualTo("studentID", Integer.parseInt(studentID));
        tagText.setText("Checking in...");

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    // User document found
                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                    boolean checkedInAD = document.getBoolean("checkedInAD");

                    if (!checkedInAD) {
                        // User is not checked in, proceed with attendance update first
                        DocumentReference attendanceRef = db.collection("eventAttendance").document("attendance");

                        attendanceRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    int filled = documentSnapshot.getLong("filled").intValue();
                                    int capacity = documentSnapshot.getLong("capacity").intValue();

                                    if (filled < capacity) {
                                        // Increment "filled" and then update user's check-in status
                                        attendanceRef.update("filled", filled + 1)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Attendance updated successfully, now update user's check-in status
                                                        document.getReference().update("checkedInAD", true)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        tagText.setText("Ready To Scan...");
                                                                        Toast.makeText(TagReadActivity.this, "User Successfully checked in", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(TagReadActivity.this, "User Check In Failed", Toast.LENGTH_SHORT).show();
                                                                        // You might want to decrement "filled" here if user check-in fails
                                                                        attendanceRef.update("filled", filled); // Decrement filled
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(TagReadActivity.this, "Failed to update attendance", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        // Room is at capacity
                                        Toast.makeText(TagReadActivity.this, "Room is at capacity", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Attendance document not found
                                    Toast.makeText(TagReadActivity.this, "Attendance document not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(TagReadActivity.this, "Failed to fetch attendance document", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // User is already checked in
                        Toast.makeText(TagReadActivity.this, "User ALREADY CHECKED IN", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User document not found
                    Toast.makeText(TagReadActivity.this, "No Account Found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(TagReadActivity.this, "Failed to fetch user document", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void onTagDiscovered(Tag tag) {
        String output = "";
        String lineDivider = "--------------------";
        output += "NFC tag detected" + "\n";

        String studentID = "";

        // examine tag
        byte[] tagUid = tag.getId();
        output += "Tag UID length: " + tagUid.length  + " UID: " + bytesToHex(tagUid) + "\n";
        String[] techlist = tag.getTechList();
        output += lineDivider + "\n";
        output += "The TechList contains " + techlist.length + " entry/ies:" + "\n";
        for (int i = 0; i < techlist.length; i++) {
            output += "Entry " + i + ": " + techlist[i]  + "\n";
        }
        output += lineDivider + "\n";
        output += tag.toString()  + "\n";
        output += lineDivider + "\n";
        // if the tag uses the NfcA class I'm connecting the tag now this class
        // I'm trying to use the NfcA class, if it is not supported by the tag an exception is thrown
        NfcA nfcA = null;
        nfcA = NfcA.get(tag);

        if (nfcA == null) {
            output += "This tag is NOT supporting the NfcA class" + "\n";
            output += lineDivider + "\n";
        } else {
            // I'm trying to get more information's about the tag and connect to the tag
            byte[] atqa = nfcA.getAtqa();
            byte sak = (byte) nfcA.getSak();
            int maxTransceiveLength = nfcA.getMaxTransceiveLength();
            output += "-= NfcA Technology =-" + "\n";
            output += "ATQA: " + bytesToHex(atqa) + "\n";
            output += "SAK: " + byteToHex(sak) + "\n";
            output += "maxTransceiveLength: " + maxTransceiveLength + "\n";
            output += lineDivider + "\n";

            try {
                nfcA.connect();


                //TODO: this might be the worst thing I ever wrote
                // Start reading from page 4 (user memory) up to page 15 (if applicable)
                for (int page = 4; page < 16; page++) {
                    // Create a READ command for the page
                    byte[] command = new byte[] {
                            (byte) 0x30, // READ command
                            (byte) page  // Page address
                    };

                    // Send command and receive data
                    byte[] response = nfcA.transceive(command);

                    // Convert raw bytes to a readable string (if text data is stored)
                    String pageData = new String(response, StandardCharsets.UTF_8);

                    Log.d("NFC", "Page " + page + ": " + pageData);

                    if(page == 6){
                        output += pageData.replaceAll("[^[0-9]*$]", "") + "\n";

                        studentID = pageData.replaceAll("[^[0-9]*$]", "");
                    }
                }



                output += "Connected to the tag using NfcA technology" + "\n";
                output += lineDivider + "\n";
                nfcA.close();
            } catch (IOException e) {
                output += "NfcA connect to tag IOException: " + e.getMessage() + "\n";
                output += lineDivider + "\n";
            }
        }



        String finalOutput = output;
        runOnUiThread(() -> {
            tagText.setText(finalOutput); //TODO actual output goes here
        });

        checkUserIn(studentID);

        // output of the logfile to console
//        System.out.println(output);
        // a short information about the detection of an NFC tag after all reading is done
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myNfcAdapter != null) {
            if (!myNfcAdapter.isEnabled())
                showWirelessSettings();
            Bundle options = new Bundle();

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
            IntentFilter[] intentFiltersArray = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
            myNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);

            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);
            // Enable ReaderMode for all types of card and disable platform sounds
            // The option NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK is NOT set
            // to get the data of the tag after reading
            myNfcAdapter.enableReaderMode( this,
                    this::onTagDiscovered, //TODO this has been weird before, check first if crashing
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (myNfcAdapter != null) {

            myNfcAdapter.disableForegroundDispatch(this);

            myNfcAdapter.disableReaderMode(this);
        }
    }

    /**
     * If the onResume() method detects that the NFC option is not enabled this method will forward you
     * to the Settings to enable NFC.
     */
    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                // Process the tag here
                Log.d("NFC", "Tag scanned: " + tag.toString());
            }
        }
    }

    private void enableForegroundDispatch() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
            IntentFilter[] intentFiltersArray = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
        }
    }

    private void disableForegroundDispatch() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    public static String byteToHex(Byte input) {
        return String.format("%02X", input);
        //return String.format("0x%02X", input);
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuffer result = new StringBuffer();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
}
