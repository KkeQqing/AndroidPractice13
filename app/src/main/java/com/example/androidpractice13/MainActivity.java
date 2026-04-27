package com.example.androidpractice13;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnQuery;
    private ArrayList<String> contactsList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    // 运行时权限请求码
    private static final int REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 初始化视图
        listView = findViewById(R.id.list_view_contacts);
        btnQuery = findViewById(R.id.btn_query_specific);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        listView.setAdapter(adapter);

        // 2. 请求权限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);

        // 3. 加载所有联系人
        loadAllContacts();

        // 4. 实现需求(2)：查询指定电话的联系人信息
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 示例号码，请替换为你手机中实际存在的号码进行测试
                String targetNumber = "13800000000";
                findContactByPhoneNumber(targetNumber);
            }
        });
    }

    // 需求(1): 读取手机所有联系人
    private void loadAllContacts() {
        contactsList.clear();
        ContentResolver contentResolver = getContentResolver();
        // 获取联系人数据的URI
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 获取联系人ID
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                // 获取联系人姓名
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                contactsList.add("姓名: " + name);
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        }
    }

    // 需求(2): 根据电话号码查询联系人
    private void findContactByPhoneNumber(String phoneNumber) {
        ContentResolver contentResolver = getContentResolver();
        // 使用 CommonDataKinds.Phone.CONTENT_URI 可以通过号码反查联系人
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        // 查询条件：号码匹配
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
        String[] selectionArgs = new String[]{phoneNumber};

        Cursor cursor = contentResolver.query(uri, null, selection, selectionArgs, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                Toast.makeText(this, "找到联系人: " + name, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "未找到该号码的联系人", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
    }
}