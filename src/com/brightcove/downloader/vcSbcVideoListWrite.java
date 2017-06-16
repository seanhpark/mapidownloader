package com.brightcove.downloader;

import com.brightcove.downloader.util.*;
import java.util.Date;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

/**
 * Created by spark on 2017. 6. 15..
 * VC의 비디오 파일 백업을 위한 실행 클래스
 * spark@brightcove.com
 */
public class vcSbcVideoListWrite {

    private static String apiToken = "api read token with rendition url access";
    private static String videoFields = "accountId,id,referenceId,name,length,creationDate,renditions";


    // 메인함수
    public static void main(String[] args) throws Exception {

        try {

            // 리스트업 대상 비디오의 총 갯수, 가장 최근 비디오 부터 역순으로 리스트업한다
            System.out.println("How many video you want to list?(0 is all) : ");

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            String inTotal = br.readLine();

            int max_count = 1;

            if(isNumeric(inTotal)){
                max_count = Integer.parseInt(inTotal);
            }
            else{
                System.out.println("Only number required!!");
                System.exit(0);
            }

            //Mapi를 한번 호출할때 몇개씩 데이터를 갖고 올것인가. 최대 100개

            int page_size = 10;

            int page_number = 0;

            //비디오의 총 갯수를 조회
            int total_title_count = getTitleCount();

            if((max_count<total_title_count)&&(max_count !=0))
                total_title_count = max_count;

            //실제 비디오 목록을 조회
            VCApiReturns vcar = getDataStream(total_title_count,page_size);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isNumeric(String s) {

        return s.matches("\\d+");
    }

    //Mapi로 조회 되는 비디오의 총 갯수
    private static int getTitleCount() throws Exception{

        CloseableHttpClient httpclient = HttpClients.createDefault();
        int total_count = 0;

        try {

            String api = "http://api.brightcove.com/services/library?command=find_all_videos&video_fields=accountId&page_number=0&page_size=1&get_item_count=true&token="+apiToken;
            String test = "git test";
            String test2 = "git test 2";
            HttpGet httpGet = new HttpGet(api);

//            System.out.println("executing request " + httpGet.getURI());
            CloseableHttpResponse response = httpclient.execute(httpGet);

            try {

//                System.out.println(response.getStatusLine());
                HttpEntity entity = response.getEntity();

                System.out.println("----------------------------------------");

                Gson gson = new Gson();
                String strReturn = new String();

                if(entity != null) {

//                    System.out.println("Response content length: " + entity.getContentLength());
                    BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));
                    String line = "";

                    while ((line = rd.readLine()) != null) {
                        System.out.println(line);
                        strReturn += line;
                    }
                }

                VCApiReturns ar = gson.fromJson(strReturn,VCApiReturns.class);

                int l = ar.items.length;
                total_count = ar.total_count;

//                System.out.println("AccountID: "+ar.items[0].accountId+" item_count: "+ar.items.length+" total_count: "+total_count);

                EntityUtils.consume(entity);

            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpclient.close();
        }
        return total_count;
    }

    //실제 비디오 리스트 조회
    private static VCApiReturns getDataStream(int total, int size) throws Exception {

        VCApiReturns ar = new VCApiReturns();

        String fName = getListFileName()+".txt";
        FileWriter fwList = new FileWriter(fName,true);
        BufferedWriter bwList = new BufferedWriter(fwList);

        int roop_count=1;

        if(total>size){
            roop_count = ((int)total/size);
            writeLog ("Total Count: "+total+", Page Size :"+size+", Roop count : "+roop_count);
        }
        //테스트용
        //roop_count=1;

        String p = "AccountID@@VideoID@@VideoSize@@encodingRate@@VideoLength@@VideoName@@CreationDate@@Url";
        //System.out.println(p);
        bwList.write(p);
        bwList.newLine();

        for(int r=0;r<roop_count;r++){

            CloseableHttpClient httpclient = HttpClients.createDefault();

            try {

                HttpGet httpGet = new HttpGet("http://api.brightcove.com/services/library?command=find_all_videos&video_fields="+videoFields+"&media_delivery=http&page_number="+r+"&page_size="+size+"&sort_by=CREATION_DATE&sort_order=DESC&get_item_count=true&token="+apiToken);

                writeLog("Executing request " + httpGet.getURI());
                CloseableHttpResponse response1 = httpclient.execute(httpGet);

                try {
                    //writeLog(response1.getStatusLine());
                    HttpEntity entity1 = response1.getEntity();

                    writeLog("----------------------------------------");
                    Gson gson = new Gson();
                    String strReturn = new String();

                    if(entity1 != null) {

//                        System.out.println("Response content length: " + entity1.getContentLength());

                        BufferedReader rd = new BufferedReader(new InputStreamReader(entity1.getContent()));

                        String line = "";

                        while ((line = rd.readLine()) != null) {
                            // writeLog(line);
                            strReturn += line;
                        }

                        if(rd != null) rd.close();
                    }

                    ar = gson.fromJson(strReturn,VCApiReturns.class);

                    int l = ar.items.length;
                    int i = 0;

                    while (l>i){

                        String bigUrl = findBiggestRendition(ar.items[i]);

                        if (bigUrl == "NO_RENDITIONS")
                            continue;

                        String[] token = bigUrl.split("##");
                        String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date (ar.items[i].creationDate));
                        String pp = ar.items[i].accountId+"@@"+ar.items[i].id+"@@"+token[1]+"@@"+token[2]+"@@"+getLength(ar.items[i])+"@@"+ar.items[i].name+"@@"+date+"@@"+token[0];
                        bwList.write(pp);
                        bwList.newLine();
                        //System.out.println(pp);

                        i++;
                    }
//                    System.out.println("item_count: "+ar.items.length);
//                    System.out.println("page_size: "+ar.page_size);

                    EntityUtils.consume(entity1);

                } finally {
                    response1.close();
                }

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpclient.close();
            }
            writeLog((r+1) +" stream writing is done.");

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(bwList != null) bwList.close();
        if(fwList != null) fwList.close();
        System.out.println("List "+fName+" is successfully created!!");

        return ar;
    }

    // 비디오 리스트 파일 이름
    private static String getListFileName() {

        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String tmpDate = format.format(now);

        return tmpDate;
    }

    // video length
    private static String getLength(VCApiReturnItem item) {

        String strLength = "0";

        if(item.length>0){
            if (item.length<60){
                strLength = item.length+"s";
            }
            else {
                int l = item.length/1000;
                int m = l/60;
                int s = l%60;
                strLength = m+"m"+s+"s";
            }
        }

        return strLength;
    }

    //제일 큰 렌디션 찾기
    private static String findBiggestRendition(VCApiReturnItem item) {

        int l = item.renditions.length;
//        System.out.println("renditions:"+l+"check:"+item.renditions[0].videoDuration);
        String biggestUrl = "NO_RENDITIONS";
        int biggestRate = 0;
        long biggestSize =0;

        if((l>0)&&(item.renditions[0].videoDuration>0)){
            biggestUrl = item.renditions[0].url;
            biggestRate = item.renditions[0].encodingRate;
            biggestSize = item.renditions[0].size;
            for (int i=1;i<l;i++){
                if(biggestRate<item.renditions[i].encodingRate){
                    biggestRate = item.renditions[i].encodingRate;
                    biggestUrl = item.renditions[i].url;
                    biggestSize = item.renditions[i].size;
                }
            }
        }
        return biggestUrl+"##"+biggestSize+"##"+biggestRate;
    }

    private static void writeLog(String s) throws Exception{

        String logFileName = "listlog.txt";

        FileWriter fwLog = new FileWriter(logFileName,true);
        BufferedWriter bwLog = new BufferedWriter(fwLog);

        //System.out.println(p);
        s = getLogDate()+" -- "+s;
        bwLog.newLine();bwLog.write(s);
        System.out.println(s);

        if(bwLog != null) bwLog.close();
        if(fwLog != null) fwLog.close();

    }

    private static String getLogDate() {

        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd-HH:mm:ss");
        String tmpDate = format.format(now);

        return tmpDate;
    }
}
