package com.brightcove.downloader;

import com.brightcove.downloader.util.*;

import java.util.Date;
import java.io.*;

import java.util.Vector;

import java.text.SimpleDateFormat;
import java.lang.System;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Created by spark on 2017. 6. 15..
 * java -server -Xms512m -Xmx512m vcSbcVideoFileDown
 * SBC의 비디오 파일 백업을 위한 실행 클래스
 * spark@brightcove.com
 * 1. vcSbcVideoListWrite로 작성된 비디오 리스트 이름 넣기
 * 2. 시작할 첫번째 레코드
 * 3. 시작 레코드부터 다운받을 총 갯수.
 *
 *
 */
public class vcSbcVideoFileDown {

    // 메인함수
    public static void main(String[] args) throws Exception {

        try {

            // 리스트업 대상 비디오의 총 갯수, 가장 최근 비디오 부터 역순으로 리스트업한다
            System.out.println("Enter the list file name to download : ");

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            //리스트를 읽어들여 배열에 저장
            String fileListName = br.readLine();

            if(fileListName == null){
                System.out.println("Please enter file name correctly!");
                System.exit(0);
            }

            // 처음 시작하는 번호 확인
            System.out.println("What is starting number? 1 is first: ");

            br = new BufferedReader(new InputStreamReader(System.in));

            String inStart = br.readLine();

            int start_count = 1;

            if(isNumeric(inStart)){
                start_count = Integer.parseInt(inStart);
                if(start_count == 0)
                    start_count =1;
            }
            else{
                System.out.println("Only number required!!");
                System.exit(0);
            }

            // 다운로드 받을 총 갯수
            System.out.println("How many video you want to download? (0=all): ");

            br = new BufferedReader(new InputStreamReader(System.in));

            String inCount = br.readLine();

            int down_count = 0;

            if(isNumeric(inCount)){
                down_count = Integer.parseInt(inCount);
            }
            else{
                System.out.println("Only number required!!");
                System.exit(0);
            }
            writeLog("Start from : "+start_count+", Download titiles: "+down_count);

            Vector<VCListItem> list = getVideoList(fileListName);

            int video_count = start_count+down_count-1;

            if (video_count >list.size())
                video_count = list.size();

            //배열의 파일을 다운로드

            writeLog("Total Video Count :"+list.size() );

            int nullCount =0;

            String p = "AccountID@@VideoID@@VideoSize@@encodingRate@@VideoLength@@VideoName@@CreationDate@@Url";
            //System.out.println(p);
            errorList(p);

            for (int i=start_count; i<=video_count;i++)
            {
                String fileVideoName = list.elementAt(i-1).id+".mp4";

                System.out.println("File name : "+fileVideoName);

                long lngRtn = getVideoFile(fileVideoName, list.elementAt(i-1).url);

                if(lngRtn <0){
                    writeLog("Null Video retried!!! : "+ fileVideoName);

                    String pp = list.elementAt(i-1).accountId+"@@"+list.elementAt(i-1).id+"@@"+list.elementAt(i-1).size+"@@"+list.elementAt(i-1).encodingRate+"@@"+list.elementAt(i-1).length+"@@"+list.elementAt(i-1).name+"@@"+list.elementAt(i-1).creationDate+"@@"+list.elementAt(i-1).url;
                    errorList(pp);
                }else if(lngRtn == 1){
                    writeLog("Exception Occurred!!! : "+ fileVideoName);

                    String pp = list.elementAt(i-1).accountId+"@@"+list.elementAt(i-1).id+"@@"+list.elementAt(i-1).size+"@@"+list.elementAt(i-1).encodingRate+"@@"+list.elementAt(i-1).length+"@@"+list.elementAt(i-1).name+"@@"+list.elementAt(i-1).creationDate+"@@"+list.elementAt(i-1).url;
                    errorList(pp);
                }else{
                    writeLog(i+" Title Downloaded! Return: "+lngRtn);
                }

                writeLog("----------------------------------------");

                System.out.println();

            }

        } catch (Exception e) {
            e.printStackTrace();
            writeLog(e.toString());
        }
    }

    public static boolean isNumeric(String s) {

        return s.matches("\\d+");
    }

    //Mapi로 조회 되는 비디오의 총 갯수
    private static Vector<VCListItem> getVideoList(String fileName) throws Exception{

        BufferedReader br = null;
        Vector<VCListItem> videoList = new Vector<VCListItem>();

        try {
            String s = null;

            br = new BufferedReader(new FileReader(fileName));

            s=br.readLine();

            if(s==null)
                s=br.readLine();

            System.out.println(s);

            int linecount=0;

            while((s=br.readLine())!=null){


                String[] token = s.split("@@");
                VCListItem newItem = new VCListItem();

                newItem.accountId = token[0];
                newItem.id = token[1];
                newItem.size = Long.parseLong(token[2]);
                newItem.encodingRate = Integer.parseInt(token[3]);
                newItem.length = token[4];
                newItem.name = token[5];
                newItem.creationDate = token[6];
                newItem.url = token[7];

                if(newItem.url=="NO_RENDITIONS")
                    continue;

                videoList.addElement(newItem);
                linecount++;

                //System.out.println(newItem.id);
                //System.out.println(newItem.name);
                //System.out.println(newItem.url);
            }

            writeLog("Total video count in list is : "+ linecount);

        }catch(Exception e){
            e.printStackTrace();
            writeLog(e.toString());
        }finally{
            if(br !=null) try{br.close();}catch(IOException e){}
            System.gc();
        }
        return videoList;
    }

    private static long getVideoFile(String fileVideoName, String fileUrl) throws Exception{

        CloseableHttpClient httpclient = HttpClients.createDefault();
        long lngRtn =0;

        try {

            HttpGet httpGet = new HttpGet(fileUrl);

            writeLog("VideoID: "+fileVideoName+", executing request " + httpGet.getURI());

            CloseableHttpResponse response = httpclient.execute(httpGet);

            try {

                System.out.println(response.getStatusLine());
                HttpEntity entity = response.getEntity();


                if(entity != null) {

                    lngRtn = entity.getContentLength();

                    writeLog("Response content length: " + entity.getContentLength());

                    if (entity.getContentLength() >0)
                    {
                        //BufferedInputStream input = new BufferedInputStream(entity.getContent());
                        InputStream input = entity.getContent();

                        FileOutputStream foutput = new FileOutputStream(new File("./mp4/"+fileVideoName));

                        //                    OutputStream output = new BufferedOutputStream(foutput);
                        OutputStream output = foutput;

                        byte[] bytesArray = new byte[4096];
                        int bytesRead = -1;

                        long count = 0L;
                        //                    long count = entity.getContentLength()/4096L;
                        //System.out.println("count = "+count);
                        long o =0L;

                        while ((bytesRead = input.read(bytesArray,0,4096)) != -1) {

                            output.write(bytesArray, 0, bytesRead);

                            o=o+bytesRead;

                            System.out.print(o+"bytes \r");
                        }
                        writeLog("Input Size :"+entity.getContentLength()+", Write Size :"+ o);

                        if(entity.getContentLength() != o){
                            writeLog("Size Unmatched and it should be download again");
                            lngRtn = -1;
                        }
                        writeLog(fileVideoName + " file writing is done!!");

                        if(output!=null) output.close(); output = null;
                        if(foutput!=null) foutput.close(); foutput = null;
                        if(input!=null) input.close();  input = null;
                    }
                }

                EntityUtils.consume(entity);

            } finally {/*
                if(output!=null) output.close();
                if(foutput!=null) foutput.close();
                if(input!=null) input.close();
                */
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeLog(e.toString());
            lngRtn = 1;
        } finally {
            httpclient.close();
        }
        return lngRtn;
    }

    private static void writeLog(String s) throws Exception{

        String logFileName = "filelog.txt";

        FileWriter fwLog = new FileWriter(logFileName,true);
        BufferedWriter bwLog = new BufferedWriter(fwLog);

        //System.out.println(p);
        s = getLogDate()+" -- "+s;
        bwLog.newLine();bwLog.write(s);
        System.out.println(s);

        if(bwLog != null) bwLog.close();
        if(fwLog != null) fwLog.close();

    }

    private static void errorList(String s) throws Exception{

        String logFileName = "errorlist.txt";

        FileWriter fwLog = new FileWriter(logFileName,true);
        BufferedWriter bwLog = new BufferedWriter(fwLog);

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
