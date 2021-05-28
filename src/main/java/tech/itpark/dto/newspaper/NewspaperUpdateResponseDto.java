package tech.itpark.dto.newspaper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NewspaperUpdateResponseDto {
    private long id;
    private long authorId;
    private String title;
    private String content;
}
