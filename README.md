# GoogleMap_TimeLine_navigation
Google Maps and Sensor Listview Project
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

        return null;
    }
    ```
