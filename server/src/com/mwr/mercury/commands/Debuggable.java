// License: Refer to the README in the root directory

package com.mwr.mercury.commands;

import java.util.List;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.mwr.mercury.ArgumentWrapper;
import com.mwr.mercury.Common;
import com.mwr.mercury.Session;

public class Debuggable
{
	public static void info(List<ArgumentWrapper> argsArray, Session currentSession){
		//Assign filter if one came in the arguments
		String filter = Common.getParamString(argsArray, "filter");
		
		//String to return at the end of function
		String returnValue = "";
		
		//Get all packages from packagemanager
		List <PackageInfo> packages = currentSession.applicationContext.getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);
		
		//Iterate through packages
		for (PackageInfo package_:packages)
		{
			ApplicationInfo app = package_.applicationInfo;
			
			//Focus on debuggable apps only and apply filter
			if (((app.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) && (app.packageName.toUpperCase().contains(filter.toUpperCase()) || app.processName.toUpperCase().contains(filter.toUpperCase()) || filter == ""))
            {
				returnValue += "Package name: " + app.packageName + "\n";
				returnValue += "UID: " + app.uid + "\n";
				
            	//Find all permissions that this app has
            	String strPermissions = "";
            	String[] permissions = package_.requestedPermissions;
            	
            	if (permissions != null)
            	{
            		for (String permission:permissions)
            			strPermissions += permission + "; ";
            	}
            	
            	returnValue += "Permissions: " + strPermissions + "\n\n";
            }
		}
		
		currentSession.sendFullTransmission(returnValue.trim(), "");
	}

}
