package com.example.verflix;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnGoToMenu, btnChangeUser;
    private static final String USERS_FILE_NAME = "users_list.txt";
    private static final String CURRENT_USER_FILE_NAME = "current_user.txt";
    private ArrayList<String> usersList = new ArrayList<>();
    private String currentUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnGoToMenu = findViewById(R.id.btnGoToMenu);
        btnChangeUser = findViewById(R.id.btnChangeUser);

        usersList = readUsersListFromFile();
        currentUserData = readFromFile(CURRENT_USER_FILE_NAME);

        boolean isRegistered = currentUserData != null && !currentUserData.isEmpty();

        if (isRegistered) {
            btnChangeUser.setVisibility(View.VISIBLE);
        }

        btnGoToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegistrationDialog();
            }
        });

        btnChangeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserSelectionDialog();
            }
        });
    }

    private void showRegistrationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_registration, null);
        builder.setView(dialogView);

        final EditText etName = dialogView.findViewById(R.id.etName);
        final EditText etAge = dialogView.findViewById(R.id.etAge);
        final RadioGroup rgGender = dialogView.findViewById(R.id.rgGender);

        builder.setTitle(R.string.registro)
                .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String name = etName.getText().toString();
                        int age = Integer.parseInt(etAge.getText().toString());
                        int selectedGenderId = rgGender.getCheckedRadioButtonId();
                        RadioButton selectedGender = dialogView.findViewById(selectedGenderId);
                        String gender = selectedGender.getText().toString();

                        String userData = name + "," + age + "," + gender;
                        writeToFile(name + ".txt", userData);
                        addToUsersList(name);
                        writeToFile(CURRENT_USER_FILE_NAME, userData);

                        goToMenu(age);
                    }
                })
                .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("StringFormatInvalid")
    private void showUserSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_user);

        ListView userList = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usersList);
        userList.setAdapter(adapter);

        builder.setView(userList);
        AlertDialog dialog = builder.create();

        userList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUser = usersList.get(position);
            String userData = readFromFile(selectedUser + ".txt");
            writeToFile(CURRENT_USER_FILE_NAME, userData);
            currentUserData = userData;

            String[] userDataParts = userData.split(",");
            int age = Integer.parseInt(userDataParts[1]);

            dialog.dismiss();
            goToMenu(age);
        });

        userList.setOnItemLongClickListener((parent, view, position, id) -> {
            String selectedUser = usersList.get(position);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_user)
                    .setMessage(getString(R.string.confirm_delete, selectedUser))
                    .setPositiveButton(R.string.aceptar, (dialog1, which) -> {

                        usersList.remove(position);
                        adapter.notifyDataSetChanged();
                        writeToFile(USERS_FILE_NAME, String.join(",", usersList));

                        deleteFile(selectedUser + ".txt");

                        if (selectedUser.equals(currentUserData.split(",")[0])) {
                            writeToFile(CURRENT_USER_FILE_NAME, "");
                            currentUserData = "";
                        }
                    })
                    .setNegativeButton(R.string.cancelar, null)
                    .show();

            return true;
        });

        dialog.show();
    }

    private void goToMenu(int age) {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        intent.putExtra("user_age", age);
        startActivity(intent);
        finish();
    }

    private void writeToFile(String fileName, String data) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String readFromFile(String fileName) {
        FileInputStream fis = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            fis = openFileInput(fileName);
            int character;
            while ((character = fis.read()) != -1) {
                stringBuilder.append((char) character);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

    private ArrayList<String> readUsersListFromFile() {
        String data = readFromFile(USERS_FILE_NAME);
        if (data != null && !data.isEmpty()) {
            String[] usersArray = data.split(",");
            Set<String> usersSet = new HashSet<>(Arrays.asList(usersArray)); // Eliminar duplicados
            return new ArrayList<>(usersSet);
        }
        return new ArrayList<>();
    }

    private void addToUsersList(String userName) {
        if (!usersList.contains(userName)) { // Evitar duplicados
            usersList.add(userName);
            writeToFile(USERS_FILE_NAME, String.join(",", usersList));
        }
    }
}
