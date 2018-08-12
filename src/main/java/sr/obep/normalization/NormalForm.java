package sr.obep.normalization;

import sr.obep.data.events.Content;

import java.util.List;
import java.util.Map;

public interface NormalForm {

    List<Map<String, Object>> apply(Content c);


}
