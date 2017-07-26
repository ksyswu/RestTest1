package com.example.pc.resttest1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URL;

public class MemUpdateActivity extends AppCompatActivity {

    private EditText edtJoinName, edtJoinId, edtJoinPw, edtJoinHp;
    private ImageView mImgProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mem_update);

        //데이터를 받아옴
        com.example.pc.resttest1.MemberBean.MemberBeanSub memberBean =
                (com.example.pc.resttest1.MemberBean.MemberBeanSub) getIntent().getSerializableExtra("memberBean"); //키값이 멤버빈

        edtJoinName = (EditText) findViewById(R.id.edtJoinName);
        edtJoinId = (EditText) findViewById(R.id.edtJoinId);
        edtJoinPw = (EditText) findViewById(R.id.edtJoinPw);
        edtJoinHp = (EditText) findViewById(R.id.edtJoinHp);
        mImgProfile = (ImageView) findViewById(R.id.imgProfile);


        edtJoinName.setText(memberBean.getName());
        edtJoinId.setText(memberBean.getUserId());
        edtJoinPw.setText(memberBean.getUserPw());
        edtJoinHp.setText(memberBean.getHp());

        new ImageLoaderTask(mImgProfile).execute(Constants.BASE_URL + memberBean.getProfileImg());

        //정보수정버튼 이벤트 등록
        findViewById(R.id.btnJoinOk).setOnClickListener(btnJoinOkClick);

    }//end onCreate

    private View.OnClickListener btnJoinOkClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //회원정보 수정
            new UpdateMemberTask().execute();
        }
    };

    class UpdateMemberTask extends AsyncTask<String, Void, String> {

        private String URL_MEMBER_UPDATE = Constants.BASE_URL + "/rest/updateMember.do";

        String strJoinId, strJoinName, strJoinHp, strJoinPw;

        @Override
        protected void onPreExecute() {
            strJoinId = edtJoinId.getText().toString();
            strJoinName = edtJoinName.getText().toString();
            strJoinPw = edtJoinPw.getText().toString();
            strJoinHp = edtJoinHp.getText().toString();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

                MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
                map.add("userId", strJoinId);
                map.add("name", strJoinName);
                map.add("userPw", strJoinPw);
                map.add("hp", strJoinHp);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);

                return restTemplate.postForObject(URL_MEMBER_UPDATE, request, String.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Gson gson = new Gson();
            try{
                MemberBean bean = gson.fromJson(s, MemberBean.class);
                if(bean != null){
                    if (bean.getResult().equals("ok")){
                        finish();
                    }else{
                        Toast.makeText(MemUpdateActivity.this, bean.getResultMsg(), Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (Exception e){
                Toast.makeText(MemUpdateActivity.this, "파싱실패", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    };//end MemUpdate


    //이미지 비동기 로딩 Task
    class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView dispImageView;


        public ImageLoaderTask(ImageView dispImgView) {
            this.dispImageView =dispImgView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            String imgUrl = params[0];

            Bitmap bmp = null;

            try {
               bmp = BitmapFactory.decodeStream(  (InputStream)new URL(imgUrl).getContent()  );
            } catch (Exception e) {
                e.printStackTrace();
            }

            return bmp;
        }//end doInBackground()


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                //표시
                dispImageView.setImageBitmap(bitmap);
            }
        }

    };

}

