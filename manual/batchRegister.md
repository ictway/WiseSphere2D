# WFS 일괄등록 기능

## 기능
해당 기능은 기존에 WFS에 레이어를 등록하기 위해서 사용자가 수작업으로 입력했던 기존의 방식을 개선하기 위해  
theme.xml에 등록된 레이어목록을 읽어와 wfs.xml에 일괄적으로 등록하는 기능입니다.

## 사용방법
  1. 수정되기 전의 wfs.xml
    - ![phase1](https://github.com/dku32131738/WiseSphere2D/blob/master/manual/img/phase01.png)
  1. WFS 일괄등록 버튼 누름 
     - ![phase2](https://github.com/dku32131738/WiseSphere2D/blob/master/manual/img/phase02.png)
  1. wfs.xml 수정
      - ![phase3](https://github.com/dku32131738/WiseSphere2D/blob/master/manual/img/phase03.png)
  1. wfs.xml의 동일한 위치에 백업 파일 생성
## class
 **SpWFSUpdateXml**
  * theme.xml의 데이터를 읽어와 wfs.xml의 레이어 목록 변경, xml파일을 수정하기 때문에 백업 기능 구현
  * package : com.ictway.wisesphere.services.wfs
  * method
    * updateFeatureStore
      * 개요 : 실질적인 기능의 전반을 관장하는 클래스
      * parameter : void
      * return value : void
    * readXml
      * 개요 : xml파일을 입력받아 해당 파일의 정보를 Document로 반환합니다.
      * parameter : File wfsFile (theme.xml)
      * return value : Document
    * writeXml
      * 개요 : Document 객체를 xml에 저장, SpWFSUpdateXml에서는 백업파일과 wfs.xml 수정에 사용
      * parameter : Document doc,File wfsXml (doc객체를 wfsXml에 저장)
      * return value : void
  * 관련 소스
    * active.xhtml
      * 개요 : 버튼을 추가한 xhtml 파일
      * 위치 : /src/main/java/webapp/active.xhtml
    * WorkspaceBean
      * 개요 : workspace 관련 자바 빈즈
      * updateFeatureStore를 실행한 후 다시 로드 수행
      * parameter : void
      * return value : String
