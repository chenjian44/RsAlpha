error id: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/model/DcChannelMessage.java:lombok/Data#
file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/model/DcChannelMessage.java
empty definition using pc, found symbol in pc: lombok/Data#
semanticdb not found
empty definition using fallback
non-local guesses:

offset: 53
uri: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/model/DcChannelMessage.java
text:
```scala
package com.tencent.wxcloudrun.model;

import lombok.@@Data;

import java.sql.Timestamp;

@Data
public class DcChannelMessage {
    private Integer id;
    private String channelId;
    private String channelName;
    private Timestamp timestamp;
    private String user;
    private String content;
    private String contentMd5;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: lombok/Data#