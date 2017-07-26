package com.example.pc.resttest1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.List;

import static com.example.pc.resttest1.R.id.edtJoinHp;
import static com.example.pc.resttest1.R.id.edtJoinId;
import static com.example.pc.resttest1.R.id.edtJoinName;
import static com.example.pc.resttest1.R.id.edtJoinPw;

/**
 * Created by pc on 2017-07-25.
 */

public class MemberListAdapter extends BaseAdapter {

    private  Context mContext;
    private  List<MemberBean.MemberBeanSub> memberList = new ArrayList<MemberBean.MemberBeanSub>();

    public MemberListAdapter(Context context){ //List<MemberBean.MemberBeanSub> memberList
        mContext = context;
        updateMemberListTask();
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public Object getItem(int position) {
        return memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateMemberListTask(){
        new MemberListTask().execute();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) { //겟뷰에서 뿌리는것 인플렉이션

        //1.인플레이터 해라
        LayoutInflater li =
            (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //2.인플레이터 한 레이아웃에서 찾는다. 컴포넌트들을
        convertView = li.inflate(R.layout.view_member_list, null);

        ImageView imgProfile = (ImageView)convertView.findViewById(R.id.imgProfile);
        final TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
        TextView txtId = (TextView) convertView.findViewById(R.id.txtId);

        //데이터를 가져온다.
        final MemberBean.MemberBeanSub bean = memberList.get(position);

        //3.찾은 컴퍼넌트에게 데이터를 대입
        new ImageLoaderTask(imgProfile).execute(Constants.BASE_URL + bean.getProfileImg());
        txtName.setText(bean.getName());
        txtId.setText(bean.getUserId());

        //리스트뷰 클릭 이벤트 등록
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, MemUpdateActivity.class);

                intent.putExtra("memberBean", bean); //멤버빈 즉 키값이 따옴표안에 이름과 같아야 함 //클래스안에서 밖의 메소드를 가져오기에 지역변수를 가져오려면 위에 파이널을 붙여주어야 함

                mContext.startActivity(intent);

                //포지션값저장
                PrefUtil.setPref(mContext, "listViewSelection", position+"");
                //몇번째를 클릭하는것인지 알려주는것
                //Toast.makeText(mContext, position+"", Toast.LENGTH_LONG).show();

            }
        });

        return convertView;
    }



    //회원정보를 가져오는 Task
    class MemberListTask extends AsyncTask<String, Void, String>{

        private String URL_MEMBER_LIST = Constants.BASE_URL + "/rest/selectMemberList.do";

        @Override
        protected String doInBackground(String... strings) {
            try{
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

                MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);

                return restTemplate.postForObject(URL_MEMBER_LIST, request, String.class);
            }catch (Exception e){
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
                        memberList = bean.getMemberList();
                        //리스트뷰 갱신 스크롤이 움직이지 않고 데이터만 변경되는 것
                        MemberListAdapter.this.notifyDataSetChanged();
                    }
                }
            }catch (Exception e){
                Toast.makeText(mContext, "파싱실패", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    };

}
