package com.qqdemo.administrator.kuaidi2;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AAAAAAAAA";
    @InjectView(R.id.spinner)
    Spinner mSpinner;
    @InjectView(R.id.ednum)
    EditText mEdnum;
    @InjectView(R.id.btn)
    Button mBtn;
    @InjectView(R.id.listview)
    ListView mListview;
    private String mHttpArg;
    private String mHttpUrl;
    Gson mGson;
    InformationBean mInformationBean;
    private String mCom;
    public String[] map = {"sf", "sto", "yt", "yd", "tt", "ems", "zto", "ht"};
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);


        mGson = new Gson();
        mListview.setAdapter(mBaseAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCom = map[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCom = map[0];
            }
        });

    }

    static String getHtmlcontentByUrl(String urlstr, String encoding) {
        StringBuffer buffer = new StringBuffer();
        URL url = null;
        try {
            url = new URL(urlstr);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36");
            InputStreamReader in = new InputStreamReader(httpConn.getInputStream(), encoding);
            BufferedReader reader = new BufferedReader(in);
            String temp = null;
            while ((temp = reader.readLine()) != null) {
                buffer.append(temp + "\n");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    BaseAdapter mBaseAdapter = new BaseAdapter() {

        private ViewHolder mHolder = null;

        @Override
        synchronized public int getCount() {
            if (mInformationBean == null) {
                return 0;
            }
            return mInformationBean.getResult().getList().size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.list_item, null);
                mHolder = new ViewHolder(convertView);
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) convertView.getTag();
            }
            InformationBean.ResultBean.ListBean listBean = mInformationBean.getResult().getList().get(position);
            mHolder.txt.setText("时间：" + listBean.getDatetime() + "\n地址：" + listBean.getRemark() + "\n");

            return convertView;
        }
    };

    @OnClick(R.id.btn)
    public void onClick() {
        hidKeyboard();
        String no = mEdnum.getText().toString();
        mHttpUrl = "http://v.juhe.cn/exp/index?";
        mHttpArg = "key=64b973df262d45cd6f1cb17bb323a755&com=" + mCom + "&no=" + no;
        new Thread(new Runnable() {
            @Override
            synchronized public void run() {
                String jsonResult = getHtmlcontentByUrl(mHttpUrl + mHttpArg, "utf-8");
                mInformationBean = mGson.fromJson(jsonResult, InformationBean.class);
                Log.i(TAG, "run: " + mInformationBean.getResultcode());
                if (mInformationBean.getResultcode().equals("200")) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBaseAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "查询不到此快递信息", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).start();
    }

    public class ViewHolder {
        private TextView txt;


        ViewHolder(View root) {
            txt = (TextView) root.findViewById(R.id.txt);

        }

    }

    void hidKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

}

