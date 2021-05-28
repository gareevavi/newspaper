package tech.itpark.dto.newspaper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NewspaperSaveRequestDto {
    private long id;
    private String authorId;
    private String title;
    private String content;
}
