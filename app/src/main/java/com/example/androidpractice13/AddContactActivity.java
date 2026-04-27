package com.example.androidpractice13;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class AddContactActivity extends AppCompatActivity {

    private EditText etName, etPhone;
    private Button btnAdd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        btnAdd = findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewContact();
            }
        });
    }

    // 需求(3): 添加一个新的联系人
    private void addNewContact() {
        String name = etName.getText().toString();
        String phone = etPhone.getText().toString();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "请输入姓名和电话", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentResolver resolver = getContentResolver();
        // 使用事务批量操作
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // 1. 在 RawContacts 表插入一条空记录，获取新的联系人ID
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // 2. 在 Data 表插入姓名数据
        // backReference(0) 引用了上一步插入操作生成的ID
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        // 3. 在 Data 表插入电话数据
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "添加失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}