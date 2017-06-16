Media APi downloader for Java

다운로드 프로그램 사용방법

리스트 작성과 다운로드의 두 단계로 실행합니다.

I. 리스트 작성
다운로드 프로그램을 실행 하기 전에 비디오클라우드에 접속해서 다운로드 받을 비디오의 개수를 대략 파악 합니다. 제일 마지막 것부터 역순으로 1000개 일지 2000개 일지 확인 합니다.
다운로드 프로그램은 지정한 갯수에 대한 리스트를 역순으로 10개씩 찾아서 합쳐 리스트를 작성합니다.

** 참고로 사용 전에 vcSbcVideoListWrite.java 파일의 상단에 있는 media api token을 지정해야 합니다

1. 꼭 Java가 설치 되어 있어야 합니다. 1.6이상으로. 없는 경우 www.java.com으로 접속해서 최신버전을 설치 합니다. 그리고 java실행 파일이 어디서나 사용가능해야 합니다.
2. 맥에서는 Terminal을, Window에서는 DOS창을 열고 아래와 같이 실행 합니다.
3. 우선 다운로드 받을 비디오 리스트를 작성합니다.
- java vcSbcVideoListWrite 라고 입력하고 엔터를 입력합니다.
-  How many video you want to list?(0 is all) : 여기서 다운받을 리스트를 작성할 비디오 갯수를 입력 합니다. 1000 이라고 하면 마지막것부터 거꾸로 1000개까지 추출합니다.
4. 입력하고 엔터를 치면 비디오 리스트 조회를 시작하고 화면에 아래처럼 나타납니다.

How many video you want to list?(0 is all) :
20
----------------------------------------
{"items":[{"accountId":1315490670001}],"page_number":0,"page_size":1,"total_count":5283}
150330-10:14:16 -- Total Count: 20, Page Size :10, Roop count : 2
150330-10:14:16 -- Executing request http://api.brightcove.com/services/library?command=find_all_videos&video_fields=accountId,id,referenceId,name,length,creationDate,renditions&media_delivery=http&page_number=0&page_size=10&sort_by=MODIFIED_DATE&sort_order=DESC&get_item_count=true&token=HIDC2nh07qh3z5kX5iNxmRa9Q711oWuTBsPyTRxd4pXqemN-nqp5Cw..
150330-10:14:17 -- ----------------------------------------
150330-10:14:18 -- 1 stream writing is done.
150330-10:14:20 -- Executing request http://api.brightcove.com/services/library?command=find_all_videos&video_fields=accountId,id,referenceId,name,length,creationDate,renditions&media_delivery=http&page_number=1&page_size=10&sort_by=MODIFIED_DATE&sort_order=DESC&get_item_count=true&token=HIDC2nh07qh3z5kX5iNxmRa9Q711oWuTBsPyTRxd4pXqemN-nqp5Cw..
150330-10:14:21 -- ----------------------------------------
150330-10:14:22 -- 2 stream writing is done.

5.나중에 listlog.txt를 오픈해보면 리스트 작성을 위해 입출력한 내용을 볼 수 있고, 에러도 볼 수 있습니다.
6. 실행 중에 해당 폴더를 보면 20150330-101416.txt 처럼 해당 일시에 작성된 리스트 파일을 볼 수 있습니다.
7. 실행이 모두 완료되고, 에러가 없었으면 작성된 txt파일을 사용하게 됩니다.
8. 리스트 txt파일은 모든 필드가 @@로 구분되도록 되어 있습니다. 따라서 Excel에서 오픈할때 '구분자 나눔' 으로 해서 구분자를 @@로 지정하면 엑셀 파일로 변환해서 따로 사용하실 수 있습니다.


II. 작성된 리스트로 다운로드 하기
위에서 작성된 리스트를 실행해서 리스트 내의 비디오 파일을 다운받아 videoid.mp4 형식으로 저장합니다.

1. vcSbcVideoFileDown의 실행파일이 있는 폴더에 'mp4'라는 하위 폴더를 하나 추가 합니다.
2. java vcSbcVideoFileDown 라고 입력하고 엔터를 칩니다.
3. Enter the list file name to download : 라고 입력되면 사용할 리스트 명을 입력 합니다. 위 I단계에서 작성한 .txt파일을 지정합니다.
4. What is starting number? 1 is first: 에 첫번째 레코드를 지정합니다. 첫번째 비디오 (.txt의 두번째라인)부터 다운받으려면 1을 지정하고 중간에 다운받다 끊어져서 다시 실행 하는경우는 마지막 번째의 비디오의 행번호-1을 입력합니다. 잘 모르겠는 경우 43같으면 40처럼 대충 입력해도 됩니다.
5. How many video you want to download? (0=all): 4번에서 지정한 비디오 부터 그 뒤로 몇개나 받길 원하는지 입력 합니다. 처음에는 10개, 50개, 100 처럼 해보고 문제가 없으면 500, 1000개로 늘려 갑니다.
6. 실행을 할때 위에 말한것처럼 mp4폴더를 만들지 않았으면 아래처럼 모두 에러가 납니다.

java.io.FileNotFoundException: ./mp4/4132656196001.mp4 (No such file or directory)
	at java.io.FileOutputStream.open(Native Method)
	at java.io.FileOutputStream.<init>(FileOutputStream.java:206)
	at java.io.FileOutputStream.<init>(FileOutputStream.java:156)
	at vcSbcVideoFileDown.getVideoFile(vcSbcVideoFileDown.java:229)
	at vcSbcVideoFileDown.main(vcSbcVideoFileDown.java:112)

7. 정상적으로 실행되면 아래 처럼 실행 결과가 나타납니다. 보면 리스트의 총갯수가 나오고, 비디오 별로 이름, url, 사이즈, 몇번째 타이틀을 다운받았는지 등이 나옵니다. 다운중에 끊기거나 사이즈가 맞지 않는 경우 에러가 나고 errorlist.txt 에 해당 비디오 정보가 저장됩니다.

150330-10:28:27 -- Start from : 1, Download titiles: 10
AccountID@@VideoID@@VideoSize@@encodingRate@@VideoLength@@VideoName@@CreationDate@@Url
150330-10:28:27 -- Total video count in list is : 2492
150330-10:28:27 -- Total Video Count :2492
AccountID@@VideoID@@VideoSize@@encodingRate@@VideoLength@@VideoName@@CreationDate@@Url

File name : 4131853194001.mp4
150330-10:28:28 -- VideoID: 4131853194001.mp4, executing request http://cheil.bc.uds.edgesuite.net/1315490670001/201503/737/1315490670001_4132009812001_150325-SDI---.mp4
HTTP/1.1 200 OK
150330-10:28:28 -- Response content length: 247687502
150330-10:31:01 -- Input Size :247687502, Write Size :247687502
150330-10:31:02 -- 4131853194001.mp4 file writing is done!!
150330-10:31:02 -- 2 Title Downloaded! Return: 247687502
150330-10:31:02 -- ----------------------------------------

File name : 4134562465001.mp4
150330-10:31:02 -- VideoID: 4134562465001.mp4, executing request http://cheil.bc.uds.edgesuite.net/1315490670001/201503/927/1315490670001_4134658304001_--------------0320.mp4
HTTP/1.1 200 OK
150330-10:31:02 -- Response content length: 91753272
72216880bytes

8. 다운중이나 완료 후에 filelog.txt를 열어 보면 다운하면서 출력된 로그를 볼 수 있고, Exception이나 Null로 찾아 보면 에러가 난 비디오를 찾을 수 있습니다. 이 비디오 들은 모두 다시 다운받는것이 안전 합니다. errorlist.txt를 열어보면 에러가 나서 다운에 문제가 있었던 모든 비디오가 리스트업되어 있습니다. errorlist.txt를 list.txt처럼 이름을 변경하고 II의 프로세스를 다시 실행하면서 list.txt라고 입력하시면 에러 리스트 안에 있는 비디오 들만 다시 다운을 받게 됩니다.

9. 매번 다시 실행 하실때는 errorlist.txt를 확인하고 안에 내용이 있으면 파일 이름을 변경하거나 삭제하고 다운로드 실행해야 합니다. 안그러면 제목 라인이 여러개 생성되어 다운받을때 에러가 날 수 있습니다.

10. 다운로드가 완료 되면 폴더내의 비디오 갯수와 앞에서 작성한 리스트 파일의 라인수를 비교해서 서로 맞는지 확인합니다.

11. 백업 하실때는 리스트, 프로그램, 비디오 , 로그 모두 담아서 보관하시기 바랍니다.

사용과 관련해서 궁금하신점은 spark@brightcove.com으로 문의 주시기 바랍니다.