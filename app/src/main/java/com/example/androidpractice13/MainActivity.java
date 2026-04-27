package com.example.androidpractice13;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // 权限请求码
    private static final int REQUEST_CONTACTS_PERMISSION = 1001;

    private Button btnRead, btnQuery, btnAdd;
    private EditText etQueryPhone, etAddName, etAddPhone;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定控件
        btnRead = findViewById(R.id.btn_read_contacts);
        btnQuery = findViewById(R.id.btn_query_contact);
        btnAdd = findViewById(R.id.btn_add_contact);
        etQueryPhone = findViewById(R.id.et_phone);
        etAddName = findViewById(R.id.et_add_name);
        etAddPhone = findViewById(R.id.et_add_phone);
        tvResult = findViewById(R.id.tv_result);

        // 先检查权限
        checkContactsPermission();

        // 1. 读取所有联系人
        btnRead.setOnClickListener(v -> readAllContacts());

        // 2. 根据电话号码查询联系人
        btnQuery.setOnClickListener(v -> {
            String phone = etQueryPhone.getText().toString().trim();
            if (!phone.isEmpty()) {
                queryContactByPhone(phone);
            } else {
                Toast.makeText(MainActivity.this, "请输入电话号码", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. 添加新联系人
        btnAdd.setOnClickListener(v -> {
            String name = etAddName.getText().toString().trim();
            String phone = etAddPhone.getText().toString().trim();
            if (!name.isEmpty() && !phone.isEmpty()) {
                addNewContact(name, phone);
            } else {
                Toast.makeText(MainActivity.this, "姓名和电话不能为空", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 检查联系人读写权限
    private void checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                    REQUEST_CONTACTS_PERMISSION);
        }
    }

    // 权限申请结果回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "请开启联系人权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ===================== 1. 读取所有联系人 =====================
    private void readAllContacts() {
        StringBuilder sb = new StringBuilder();
        ContentResolver resolver = getContentResolver();

        // 查询联系人数据
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 获取联系人ID
                String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                // 获取联系人姓名
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

                sb.append("姓名：").append(name).append("\n");
                sb.append("ID：").append(contactId).append("\n");

                // 根据ID查询电话号码
                Cursor phoneCursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{contactId},
                        null);

                if (phoneCursor != null) {
                    while (phoneCursor.moveToNext()) {
                        String phone = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        sb.append("电话：").append(phone).append("\n");
                    }
                    phoneCursor.close();
                }
                sb.append("------------------------\n");
            }
            cursor.close();
        }
        tvResult.setText(sb.toString());
    }

    // ===================== 2. 根据电话查询联系人 =====================
    private void queryContactByPhone(String phoneNumber) {
        StringBuilder sb = new StringBuilder();
        ContentResolver resolver = getContentResolver();

        // 通过电话匹配联系人
        Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?",
                new String[]{"%" + phoneNumber + "%"},
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));

                sb.append("找到联系人：\n");
                sb.append("姓名：").append(name).append("\n");
                sb.append("电话：").append(phone).append("\n");
            }
            cursor.close();
        }

        if (sb.length() == 0) {
            tvResult.setText("未找到该电话对应的联系人");
        } else {
            tvResult.setText(sb.toString());
        }
    }

    // ===================== 3. 添加新联系人 =====================
    private void addNewContact(String name, String phone) {
        try {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            // 第一步：插入联系人账户
            operations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            // 第二步：插入姓名
            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build());

            // 第三步：插入电话
            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());

            // 执行批量添加
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
            Toast.makeText(this, "联系人添加成功！", Toast.LENGTH_SHORT).show();
            etAddName.setText("");
            etAddPhone.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "添加失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}