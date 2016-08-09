package macroutils;

import star.common.*;

public class MyMacro extends MacroUtils {
    
    public void execute() {
        
        initialize();
        
        for(Region r : getAllRegions()){
            
            say("Region Name: " + r.getPresentationName());
            
        }
        
    }
}

