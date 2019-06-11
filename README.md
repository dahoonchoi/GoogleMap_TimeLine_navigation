## GoogleMap_TimeLine_navigation

Google Map으로 TimeLine으로 실시간 위치를 SQLite에 저장하면서 다시돌아가는 방향을 자기장 센서로 방향을 알려준다.

#### Android의 자기장 센서를 이용하여 다시돌아가는 방향을 표현
```java
private String getDirectionFromDegrees(float degrees) {
        if (degrees >= -22.5 && degrees < 22.5) {
            return "N";
        }
        if (degrees >= 22.5 && degrees < 67.5) {
            return "NE";
        }
        if (degrees >= 67.5 && degrees < 112.5) {
            return "E";
        }
        if (degrees >= 112.5 && degrees < 157.5) {
            return "SE";
        }
        if (degrees >= 157.5 || degrees < -157.5) {
            return "S";
        }
        if (degrees >= -157.5 && degrees < -112.5) {
            return "SW";
        }
        if (degrees >= -112.5 && degrees < -67.5) {
            return "W";
        }
        if (degrees >= -67.5 && degrees < -22.5) {
            return "NW";
        }

        return null
    }
```
    
#### 내부 DB의 SQLite 를 사용하여 Select문을 DESC로 내림차순으로 반대방향이 처음으로 오게 SQL문
```java
     public Cursor getListContents3(String Disnum) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM users_data where PATHNUM='"+Disnum+"' ORDER BY ID DESC;", null);
        return data;
    }
```
