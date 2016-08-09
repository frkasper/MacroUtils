package macroutils;

import star.common.*;

public class MyMacro extends MacroUtils {
    
    public void execute() {
        
        _initUtils();
        
        for(Region r : getAllRegions()){
            
            say("Region Name: " + r.getPresentationName());
            
        }
        
    }
}

