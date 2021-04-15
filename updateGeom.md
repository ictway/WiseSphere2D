# updateGeom 서비스

## 기능  
해당 서비스는 사용자가 ws엔진을 통해 **DB에 저장되어 있는 레이어**들의 properties와 geometry 정보를 원하는 데이터로 수정을 하는 API  
**해당 서비스는 현재 ORACLE DB만 적용되고 있습니다.**

## API 목록
- [x] update
- [x] insert
- [x] delete
- [ ] delete(Geometry 기반)
  
## API 명세
### update
* method : POST
* url : http://{serverIp}:{port}/ws/services/updateGeom
* request
  * l
  
## class & interface
* GeometryServlet
  * HttpServlet을 상속
  * updateGeom 서비스를 접근하는 servlet
  * package : com.ictway.wisesphere.services.custom
  * method
    * dopost
      * 개요 :사용자가 post 메소드로 request를 보내면 discriminationAction 메소드에 HttpServletRequest를 파라미터로 보내  
    요청에 맞는 작업을 수행 후 수행된 결과를 사용자에게 제공합니다.
      * parameter : HttpServletRequest,HttpServletResponse
      * return value : void
    * discriminationAction
      * 개요 : request의 action 파라미터를 사용하여 switch문을 통해 적합한 서비스에 연결합니다.
      * parameter : HttpServletRequest
      * return value :  JsonObject**(사용자에게 제공될 메시지)**
* GeometryDBService
  * 사용자가 필요로 하는 서비스들의 interface입니다.
  * package : com.ictway.wisesphere.services.custom.service
  * method
    * process
      * 개요 : 수행되는 메소드의 순서를 제어합니다.
      * **각각의 클래스에서 오버로딩됩니다.**
      * parameter : void
      * return value : JsonObject
    * initFeatureConnection
      * 개요 : layerId를 이용하여 workspace에서 해당되는 레이어를 찾은 후 connId를 기반으로 DB와 연결됩니다.
      * parameter : String layerId
      * return value : void
    * getColumnList
      * 개요 : 요청 parameter인 cList를 이용하여 변경할 DB column 목록을 반환합니다.
      * parameter : String[] cList
      * return value : ArrayList<String>
    * getColumnListFromDB
      * 개요 : 사용자가 입력한 tableId를 이용하여 DB에 직접 접근하여 변경할 DB column 목록을 반환합니다.
      * 문제점 : 사용자 권한 문제로 인해 미완성입니다.
      * parameter : String tableId
      * return value : return value : ArrayList<String>
    * getSql
      * 개요 : DB에 전송할 query를 생성합니다.
      * **각각의 클래스에서 오버로딩 됩니다.**
      * parameter : void
      * return value : String
    * updateFeature
      * 개요 : DB에 query를 전송하여 feature를 사용자 요청에 맞게 수정합니다.
      * parameter : String sql
      * return value : int (성공 실패 여부)
    * closeConnection
      * 개요 : DB 연결 해제
      * parameter : void
      * return value : void
  * 하위 클래스
    * UpdateOracleService
      * 레이어 요소를 업데이트 하는 서비스입니다.
      * package : com.ictway.wisesphere.services.custom.service
      * method
        * process
          * GeometryDBService의 process를 오버로딩
          * parameter
            * String layerId (레이어명)
            * String tableName (테이블명)
            * String wClause (where절)
            * String json (geojson)
            * String srid (좌표계)
            * String geomName (geometry column명)
            * String[] cList (column목록)
          * **동일한 파라미터는 앞으로 생략**
        * initFeatureConnection
          * 개요 : overriding
        * getSql
          * 상속받은 getSql을 overloading
          * parameter : String layerId,String wClause,String json,String geomName,ArrayList<String> columnList,String srid
            * columList : getColumnList에서 반환된 변경할 column목록
        * updateFeature
          * 개요 : overriding
        * getColumnList
          * 개요 : overriding
        ColumnListFromDB
          * 개요 : overriding
          
    
  * 해당 인터페이스는 다음과 같은 클래스에 상속됩니다.
    * UpdateOracleService
      * 개요 : 사용자의 요청을 기반으로 update문을 만들어 레이어에 feature를 추가합니다.
      