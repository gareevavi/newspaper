package tech.itpark.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Newspaper {
    private long id;
    private long authorId;
    private String title;
    private String content;
//    private String timestamp;
}
