package practice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateBookRequest {
    private String title;
    private List<Long> authorIds;
}