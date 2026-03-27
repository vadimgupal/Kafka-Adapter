package practice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateAuthorRequest {
    private String name;
    private List<Long> bookIds;
}