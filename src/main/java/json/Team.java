package json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {

    private String team_name;

    public String getTeam_name() {
        return team_name;
    }
}
