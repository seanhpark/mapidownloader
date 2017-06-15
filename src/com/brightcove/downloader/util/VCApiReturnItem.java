package com.brightcove.downloader.util;

/**
 * Created by spark on 2017. 6. 15..
 * 리턴되는 json안의 아이템 정의
 */
public class VCApiReturnItem {

    public String accountId;
    public String id;
    public String referenceId;
    public String name;
    public long creationDate;
    public int length;

    public VCApiRendition renditions[];

    VCApiReturnItem() {

    }
}
