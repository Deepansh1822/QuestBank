package in.sfp.main.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PaperGenerationRequest {
    private String organisation;
    private String className;
    private String subjectName;
    private String instructions;
    private String examType;
    private String totalTime;
    private String setNumber;
    private List<SectionRequest> sections = new ArrayList<>();

    public PaperGenerationRequest() {
    }
}
