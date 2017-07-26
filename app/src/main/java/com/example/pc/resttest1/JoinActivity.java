package com.example.pc.resttest1;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class JoinActivity extends AppCompatActivity {

    private EditText edtJoinName, edtJoinId, edtJoinPw, edtJoinHp;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        edtJoinName= (EditText) findViewById(R.id.edtJoinName);
        edtJoinId = (EditText) findViewById(R.id.edtJoinId);
        edtJoinPw= (EditText) findViewById(R.id.edtJoinPw);
        edtJoinHp = (EditText) findViewById(R.id.edtJoinHp);

        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        //회원가입 버튼 클릭 처리
        findViewById(R.id.btnJoinOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new JoinTask().execute();
            }
        });
    } //onCreate end


    //회원가입 처리
    private  class JoinTask extends AsyncTask<String,Void, String> {//parameter순서대로 doingbackground에 넘어가는것 update progress / onpostexecute

        public static final String URL_JOIN_PROC = "http://117.17.93.202:8383/rest/insertMember.do";

        private  String name, userId, userPw, hp;

        @Override
        protected void onPreExecute(){

            //프로그레스 다이얼로그 표시
            mProgressBar.setVisibility(View.VISIBLE);

            //에딧 텍스트는 스레드에 접근할 수 업기에 onpreExecute에 넣어야함
            name = edtJoinName.getText().toString();
            userId = edtJoinId.getText().toString();
            userPw = edtJoinPw.getText().toString();
            hp = edtJoinHp.getText().toString();

        }



        @Override
        protected String doInBackground(String... params) {
            try{
                RestTemplate restTemplate = new RestTemplate();
                //restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

                MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
                map.add("name", name);
                map.add("userId", userId);
                map.add("userPw", userPw);
                map.add("hp", hp);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);

                return restTemplate.postForObject(URL_JOIN_PROC, request, String.class);
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            //완료되면 프로그레서 빙글 도는것 없애기
            mProgressBar.setVisibility(View.INVISIBLE);

            Gson gson = new Gson();
            try{
                MemberBean bean = gson.fromJson(s, MemberBean.class);
                if(bean != null){
                    if (bean.getResult().equals("ok")){
                        //회원가입성공
                        Intent i = new Intent(JoinActivity.this, LoginSuccActivity.class);
                        startActivity(i);
                    }else{
                        //회원가입 실패
                        Toast.makeText(JoinActivity.this, bean.getResultMsg(), Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (Exception e){
                Toast.makeText(JoinActivity.this, "파싱실패", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }
    }


}
