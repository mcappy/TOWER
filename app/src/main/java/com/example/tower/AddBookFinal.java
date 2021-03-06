package com.example.tower;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddBookFinal extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    EditText titleET;
    EditText authorET;
    EditText descriptionET;
    EditText isbnET;
    EditText priceET;
    String imageUrl;
    Spinner spinner;
    String condition;
    Button addBook;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book_final);

        progress = findViewById(R.id.progressBar);
        String title = "";
        String author = "";
        String description = "";
        String isbn13 = "";
        if(getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            title = bundle.getString("BOOK_TITLE");
            author = bundle.getString("BOOK_AUTHOR");
            description = bundle.getString("BOOK_DESCRIPTION");
            isbn13 = bundle.getString("BOOK_ISBN");

            imageUrl = bundle.getString("IMAGE_URL");
        }

        titleET = findViewById(R.id.title_input);
        authorET = findViewById(R.id.author_input);
        descriptionET = findViewById(R.id.description_input);
        isbnET = findViewById(R.id.isbn_input);
        priceET = findViewById(R.id.price_input);
        spinner = findViewById(R.id.spinner);

        addBook = findViewById(R.id.add_book_button);
        titleET.addTextChangedListener(addBookTextWatcher);
        authorET.addTextChangedListener(addBookTextWatcher);
        isbnET.addTextChangedListener(addBookTextWatcher);
        priceET.addTextChangedListener(addBookTextWatcher);

        titleET.setText(title);
        authorET.setText(author);
        descriptionET.setText(description);
        isbnET.setText(isbn13);

        ArrayAdapter<CharSequence> conditionAdapter = ArrayAdapter.createFromResource(this, R.array.condition_options, android.R.layout.simple_spinner_item);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(conditionAdapter);
        spinner.setSelection(1);
        spinner.setOnItemSelectedListener(this);



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set Home Selected
        bottomNavigationView.setSelectedItemId(R.id.profile);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.search:
                        startActivity(new Intent(getApplicationContext(), Search.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.profile:
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0,0);
                        return true;
                }

                return false;
            }
        });
    }

    private TextWatcher addBookTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            String title = titleET.getText().toString();
            String author = authorET.getText().toString();
            String isbn = isbnET.getText().toString();
            String price = priceET.getText().toString();
            addBook.setEnabled(!title.isEmpty() && !author.isEmpty() && isbn.length() == 13 && !price.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void onClickSubmit(View view) throws Exception
    {
        try {
            progress.setVisibility(View.VISIBLE);
            EditText[] etArr = {titleET, authorET, isbnET, priceET, descriptionET};
            for (EditText et: etArr) {
                if (et.getText().toString().equals("")) {
                    throw new Exception("Don't leave any information blank");
                }
            }

            DatabaseReference reference = database.getReference().child("textbooks");
            String key = reference.push().getKey();
            String title = titleET.getText().toString();
            String author = authorET.getText().toString();
            String description = descriptionET.getText().toString();
            String isbn = isbnET.getText().toString();
            Double price = Double.parseDouble(priceET.getText().toString());
            final Textbook textbook = new Textbook(title, author, MainActivity.id, price, key, imageUrl, isbn, description, condition);

            if (title.length() > 100) {
                throw new Exception("Enter a shorter title");
            }

            if (description.split(" ").length > 750) {
                throw new Exception("Enter a description with less than 750 words");
            }

            if (price > 1000) {
                throw new Exception("Maximum book price is $1000");
            }


            reference.child(key).child("title").setValue(textbook.getTitle());
            reference.child(key).child("author").setValue(textbook.getAuthor());
            reference.child(key).child("price").setValue(textbook.getPrice());
            reference.child(key).child("seller").setValue(MainActivity.id);
            reference.child(key).child("description").setValue(textbook.getDescription());
            reference.child(key).child("imageUrl").setValue(textbook.getImageUrl());
            reference.child(key).child("isbn13").setValue(textbook.getIsbn13());
            reference.child(key).child("uniqueID").setValue(key);
            reference.child(key).child("condition").setValue(textbook.getCondition()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progress.setVisibility(View.GONE);
                        Intent intent = new Intent(AddBookFinal.this, ProfileLogin.class);
                        intent.putExtra("ADDED_BOOK_MESSAGE", "Added " + textbook.getTitle());
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                }
            });

        } catch (Exception e) {
            progress.setVisibility(View.GONE);
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String condition = parent.getItemAtPosition(position).toString();
        this.condition = condition;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onClickConditionButton(View view) {
        Intent intent = new Intent (this, ConditionDescription.class);
        intent.putExtra("ORIGINAL_CLASS", "ProfileLogin");
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    public void goBack(View view) {
        onBackPressed();
        overridePendingTransition(0,0);
    }


}
