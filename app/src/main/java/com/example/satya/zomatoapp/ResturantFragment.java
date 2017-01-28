package com.example.satya.zomatoapp;


import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResturantFragment extends Fragment {
    EditText et1;
    Button bt1;
    //declare required variables
    //declare required variable
    RecyclerView recyclerView;
    ArrayList<Resturant>arrayList;
    MyRecyclerViewAdapter myRecyclerViewAdapter;
    MyTask myTask;
    LinearLayoutManager linearLayoutManager;
    int pos;//user may want postion of any restirant,its only show the postion
    double curlat, curlong;
    //
    public void showPopup(View v){
        PopupMenu popupMenu =new PopupMenu(getActivity(),v) ;
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.overflowmenu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.map:
                        Intent intent = new Intent(getActivity(),MapsActivity.class);
                        Resturant resturant=arrayList.get(pos);
                        intent.putExtra("latitude",resturant.getLatitude());
                        intent.putExtra("longitude",resturant.getLongitude());
                        intent.putExtra("name",resturant.getName());
                        startActivity(intent);
                        break;
                    case R.id.web:
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }
    //prepare assynctask method
    public class MyTask extends AsyncTask<String,Void,String>{
        URL myurl;
        HttpURLConnection connection;
        InputStream inputstream;
        InputStreamReader inputstreamreader;
        BufferedReader bufferedreader;
        String line;
        StringBuilder result;

        @Override
        protected String doInBackground(String... Strings) {
            //process to connect
            try {
                myurl=new URL(Strings[0]);
                connection= (HttpURLConnection) myurl.openConnection();
                connection.setRequestProperty("Accept", "application/json");//imp question
                connection.setRequestProperty("user-key","878cd9b31b3b562b5c01f661b681c303");
                connection.connect();
                inputstream=connection.getInputStream();
                inputstreamreader=new InputStreamReader(inputstream);
                bufferedreader=new BufferedReader(inputstreamreader);
                line=bufferedreader.readLine();
                result=new StringBuilder();
                while (line!=null){
                    result.append(line);
                    line=bufferedreader.readLine();
                }
                return result.toString();//return final result (json data) to onpost execute

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("b34","url is improper");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("b34","network problem");
            }
            return "something went worng";
        }


        @Override
        protected void onPostExecute(String s)
        {

            try {
                JSONObject jsonObject =new JSONObject(s) ;
                JSONArray jsonArray = jsonObject.getJSONArray("nearby_restaurants");
                for (int i=0;i<jsonArray.length();i++)
                {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    JSONObject res= jsonObject1.getJSONObject("restaurant");
                    String name=res.getString("name");
                    JSONObject location =res.getJSONObject("location");
                    String address=location.getString("address");
                    String locality=location.getString("locality");
                    String latitude=location.getString("latitude");
                    String longitude=location.getString("longitude");
                    String thumb=res.getString("thumb");
                    Resturant resturant=new Resturant(name,locality,address,thumb,latitude,longitude) ;

                    resturant.setName(name);
                    resturant.setAddress(address);
                    resturant.setLocality(locality);
                    resturant.setImageUrl(thumb);
                    resturant.setLatitude(latitude);
                    resturant.setLongitude(longitude);

                    arrayList.add(resturant);
                }
                myRecyclerViewAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }
    }

    //recyclerview adapter,with view holder
    public  class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>{



        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v=getActivity().getLayoutInflater().inflate(R.layout.row,parent,false);
            ViewHolder viewHolder =new ViewHolder(v);
            return viewHolder;
        }



        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            Resturant r= arrayList.get(position);

            holder.tv1.setText(r.getName());
            holder.tv2.setText(r.getLocality());
            holder.tv3.setText(r.getAddress());
            holder.img2.setTag(position);//new code for tag(over flow button)
            Glide.with(ResturantFragment.this).load(r.getImageUrl()).placeholder(R.mipmap.ic_launcher).crossFade().into(holder.img1);
            holder.img2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView imageView = (ImageView) v;
                    pos= (int) imageView.getTag();
                    showPopup(v);

                }
            });
        }


        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tv1,tv2,tv3;
            ImageView img1,img2;
            public ViewHolder(View itemView) {
                super(itemView);
                tv1= (TextView) itemView.findViewById(R.id.tv1);
                tv2= (TextView) itemView.findViewById(R.id.tv2);
                tv3= (TextView) itemView.findViewById(R.id.tv3);
                img1= (ImageView) itemView.findViewById(R.id.img1);
                img2= (ImageView) itemView.findViewById(R.id.img2);
            }
        }
    }


    public ResturantFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_resturant, container, false);
        bt1= (Button) v.findViewById(R.id.bt1);
        et1= (EditText) v.findViewById(R.id.et1);
        recyclerView= (RecyclerView) v.findViewById(R.id.recyclerView1);
        arrayList=new ArrayList<Resturant>();
        myRecyclerViewAdapter=new MyRecyclerViewAdapter();
        myTask=new MyTask();
        linearLayoutManager=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        //establish all links
        recyclerView.setAdapter(myRecyclerViewAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //read address
                String address = et1.getText().toString();//user given address
                Geocoder geocoder = new Geocoder(getActivity());
                try {
                    List<Address> addresses =geocoder.getFromLocationName(address,10);
                    Address best= addresses.get(0);
                    curlat=best.getLatitude();
                    curlong=best.getLongitude();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //19.start asynctask with zomato url
                HomeActivity homeactivity= (HomeActivity) getActivity();
                if (homeactivity.checkInternet()){
                    myTask.execute("https://developers.zomato.com/api/v2.1/geocode?lat="+curlat+"&lon="+curlong);

                }else {
                    Toast.makeText(getActivity(), "CHECK INTERNET", Toast.LENGTH_SHORT).show();
                }

            }
        });
        return v;
    }
}
