/**
 * copyright 2013 ERPConsulteres Y Asociados
 * 
 * GPL v2.0
 */
package org.mb.appdroid;

import java.sql.SQLException;

import javax.jws.WebService; 

import com.erpconsultoresasociados.DataSetSQLS;
import com.erpconsultoresasociados.InitialLoadDocument;
import com.erpconsultoresasociados.InitialLoadResponseDocument;

/**
 * @author carlos Parada
 * @contributor - conversion to iDempiere : red1 / Deepak Pansheriya
 */
@WebService(endpointInterface="org.mb.appdroid.AppDroidServices", serviceName="ADInterface/services/AppDroidServices",targetNamespace="http://www.erpconsultoresasociados.com/")
public class AppDroidServicesImpl extends MAppDroidServicesImpl implements AppDroidServices {
	
	/*
	 * @author Carlos
	 * @date 01/05/2012
	 * @time 14:04:32
	 * @type AppDroidServices
	 * @param
	 * @description
	 * @return
	 * @see org.mb.appdroid.AppDroidServices#initLoad(java.lang.String, java.lang.String)
	 */
	@Override
	public InitialLoadResponseDocument InitialLoad(InitialLoadDocument req)
			 {
		// TODO Auto-generated method stub
		InitialLoadResponseDocument resp;
		
		if (validateuser(req))
		{
			try {
				resp = initialLoad();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				resp = InitialLoadResponseDocument.Factory.newInstance();
				DataSetSQLS ds = resp.addNewInitialLoadResponse();
				ds.setError(e.getMessage());
				 			}
		}
		else
		{
			resp = InitialLoadResponseDocument.Factory.newInstance();
			DataSetSQLS ds = resp.addNewInitialLoadResponse();
			ds.setError("Username or Password Incorrect");
 			
		}
				
		return resp;
	}

	/*
	 * @author Carlos Parada
	 * @date 01/05/2012
	 * @time 14:14:10
	 * @type AppDroidServices
	 * @param
	 * @description
	 * @return
	 * @see org.mb.appdroid.AppDroidServices#getVersion()
	 */
	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "1.0";
	}
}
