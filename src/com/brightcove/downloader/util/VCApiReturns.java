package com.brightcove.downloader.util;

/**
 * Created by spark on 2017. 6. 15..
 * Mapi의 리턴 json 정의
 */
public class VCApiReturns {

    public int page_number;
    public int page_size;
    public int total_count;

    public VCApiReturnItem items[];

    public VCApiReturns() {

    }
}
