package tw.org.iii.yichun.foodsharing;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Calendar;

public class AddFoodActivity extends AppCompatActivity {
    private TextInputEditText datetime;
    private Button datePickerBtn;
    private ImageView foodimg;
    private File sdroot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        datetime = findViewById(R.id.addFood_datetime);
        datePickerBtn = findViewById(R.id.datePickerBtn);

        foodimg = findViewById(R.id.addFood_img);
        sdroot = Environment.getExternalStorageDirectory();
        Bitmap bmp = BitmapFactory.decodeFile(sdroot.getAbsolutePath() + "/iii02.jpg");
        foodimg.setImageBitmap(bmp);



        datePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatetimePicker();
            }
        });


        FoodCategoryList();
        FoodCityList();

    }

    public void FoodCategoryList(){
        Resources resources = getResources();
        String[] list = resources.getStringArray(R.array.foodCategory);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_menu_popup_item,
                        list);

        AutoCompleteTextView editTextFilledExposedDropdown = findViewById(R.id.addFood_category);
        editTextFilledExposedDropdown.setAdapter(adapter);
    }

    public void FoodCityList(){
        Resources resources = getResources();
        String[] list = resources.getStringArray(R.array.foodCity);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_menu_popup_item,
                        list);

        AutoCompleteTextView editTextFilledExposedDropdown = findViewById(R.id.addFood_city);
        editTextFilledExposedDropdown.setAdapter(adapter);
    }

    public void showDatetimePicker(){
        DatePickerDialog dialog = new DatePickerDialog(this,
                DatePickerDialog.THEME_DEVICE_DEFAULT_DARK, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                datetime.setText(year + "/" + (month+1) + "/" + dayOfMonth);
            }
        },
                2020, 4 - 1, 12);

        DatePicker picker = dialog.getDatePicker();
        Calendar limit = Calendar.getInstance();
        limit.set(2020,3,12);

        picker.setMinDate(limit.getTimeInMillis());

        dialog.show();
    }

    public void toAddFoodPreview(View view) {

    }
}