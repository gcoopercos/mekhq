package mekhq.campaign.stratcon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import megamek.common.Compute;
import megamek.common.Coords;
import megamek.common.UnitType;
import mekhq.campaign.Campaign;
import mekhq.campaign.force.Force;
import mekhq.campaign.force.Lance;
import mekhq.campaign.mission.AtBContract;
import mekhq.campaign.mission.Contract;
import mekhq.campaign.mission.ScenarioMapParameters.MapLocation;

public class StratconTrackState {
    
    // a track has the following characteristics:
    // width/height
    // [future]: terrain information by coordinates
    // scenario information by coordinates
    // active facilities by coordinates
    private String displayableName;
    private int width;
    private int height;
    private Map<Coords, StratconFacility> facilities;
    private Map<Coords, StratconScenario> scenarios;
    private Set<Integer> assignedForceIDs;
    private int scenarioOdds;
    private int deploymentTime;
    private int requiredLanceCount;
    
    public StratconTrackState() {
        facilities = new HashMap<>();
        scenarios = new HashMap<>();
        setAssignedForceIDs(new HashSet<>());
    }
    
    public String getDisplayableName() {
        return displayableName;
    }
    
    public void setDisplayableName(String name) {
        displayableName = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Map<Coords, StratconFacility> getFacilities() {
        return facilities;
    }

    public void setFacilities(Map<Coords, StratconFacility> facilities) {
        this.facilities = facilities;
    }

    public StratconFacility getFacility(Coords coords) {
        return facilities.get(coords);
    }
    
    public Map<Coords, StratconScenario> getScenarios() {
        return scenarios;
    }

    public void setScenarios(Map<Coords, StratconScenario> scenarios) {
        this.scenarios = scenarios;
    }
    
    public StratconScenario getScenario(Coords coords) {
        return scenarios.get(coords);
    }
    
    public int getRequiredLanceCount() {
        return requiredLanceCount;
    }

    public void setRequiredLanceCount(int requiredLanceCount) {
        this.requiredLanceCount = requiredLanceCount;
    }
    
    public int getDeploymentTime() {
        return deploymentTime;
    }

    public void setDeploymentTime(int deploymentTime) {
        this.deploymentTime = deploymentTime;
    }

    public int getScenarioOdds() {
        return scenarioOdds;
    }

    public void setScenarioOdds(int scenarioOdds) {
        this.scenarioOdds = scenarioOdds;
    }

    public void assignForce(int forceID) {
        getAssignedForceIDs().add(forceID);
    }
    
    public void unassignForce(int forceID) {
        getAssignedForceIDs().remove(forceID);
    }
    
    /**
     * Returns the set of all force IDs assigned to this track, regardless of scenario deployment status
     */
    public Set<Integer> getAssignedForceIDs() {
        return assignedForceIDs;
    }

    public void setAssignedForceIDs(Set<Integer> assignedForceIDs) {
        this.assignedForceIDs = assignedForceIDs;
    }
    
    public void addFacility(Coords coords, StratconFacility facility) {
        facilities.put(coords, facility);
    }
    
    public void removeFacility(Coords coords) {
        facilities.remove(coords);
    }
    
    /**
     * Returns the set of all force IDs for forces assigned to this track
     * that have not been assigned to a scenario already.
     * @return
     */
    public List<Integer> getAvailableForceIDs() {
        List<Integer> retVal = new ArrayList<>();
        
        for(int forceID : assignedForceIDs) {
            boolean forceAssigned = false;
            
            for(StratconScenario scenario : scenarios.values()) {
                if(scenario.getAssignedForces().contains(forceID)) {
                    forceAssigned = true;
                    break;
                }
            }
            
            if(!forceAssigned) {
                retVal.add(forceID);
            }
        }
        
        return retVal;
    }
    
    @Override
    public String toString() {
        return getDisplayableName();
    }
}
